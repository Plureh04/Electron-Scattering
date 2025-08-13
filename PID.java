// Data is filtered to include only events with both an electron and a proton.

import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.groot.math.F1D;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import org.jlab.clas.physics.*;
import org.jlab.groot.data.GraphErrors;

import java.util.ArrayList;

public class PID {

    public static void main(String[] args) {

        // Set up the HipoChain reader and add input files
        HipoChain reader = PB_Files.getPBChain();
        reader.open();

        Event event = new Event();
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // 2D histogram: Beta vs Momentum for all particles
        H2F BvM = new H2F("hBetaVsPTot", 100, 0.3, 6.0, 100, 0.1, 1.05);
        BvM.setTitleX("P [GeV]");
        BvM.setTitleY("#beta");

        // 2D histogram: DeltaBeta vs Momentum for protons
        H2F DBvM = new H2F("DBvM", 25, 0.60, 3.0, 100, -0.012, 0.012);
        DBvM.setTitleX("P [GeV]");
        DBvM.setTitleY("D#beta");

        Vector3 v3Parts = new Vector3(0.0, 0.0, 0.0);

        // Canvases for visualization
        TCanvas ec = new TCanvas("BvM", 600, 600);
        TCanvas ec2 = new TCanvas("DBvM", 600, 600);

        int protonCount = 0; // Number of events with both electron and proton
        int eventCount = 0;  // Total number of events processed

        // Mass of proton in GeV
        float mass = 0.938f;

        // Event loop: process all events in the input files
        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);

            boolean hasElectron = false;
            boolean hasProton = false;

            // Check if the event contains both an electron and a proton
            if (particles.getRows() > 0) {
                for (int i = 0; i < particles.getRows(); i++) {
                    int pid = particles.getInt("pid", i);

                    if (pid == 11) {
                        hasElectron = true;
                    }
                    if (pid == 2212) {
                        hasProton = true;
                    }
                }

                // Only process events with both electron and proton
                if (hasElectron && hasProton) {
                    for (int i = 0; i < particles.getRows(); i++) {
                        int pid = particles.getInt("pid", i);
                        int charge = particles.getInt("charge", i);

                        // Only consider particles with positive beta
                        if (particles.getFloat("beta", i) > 0) {
                            v3Parts.setXYZ(
                                    particles.getFloat("px", i),
                                    particles.getFloat("py", i),
                                    particles.getFloat("pz", i));
                            float momentum = (float) v3Parts.mag();
                            float observedBeta = particles.getFloat("beta", i);

                            // Fill Beta vs Momentum histogram for all particles
                            BvM.fill(momentum, observedBeta);

                            if (pid == 2212) {
                                // For protons: calculate expected beta and deltaBeta
                                float expectedBeta = momentum / (float) Math.sqrt(Math.pow(momentum, 2) + Math.pow(mass, 2));
                                float deltaBeta = observedBeta - expectedBeta;

                                // Fill DeltaBeta vs Momentum histogram for protons within deltaBeta range
                                if (deltaBeta >= -0.03 && deltaBeta <= 0.03) {
                                    DBvM.fill(momentum, deltaBeta);
                                }
                            }
                        }
                    }
                    protonCount++; // Count events with both electron and proton
                }
                eventCount++; // Count all events processed
            }
        }

        // Draw and save Beta vs Momentum histogram
        ec.draw(BvM);
        ec.save("/home/youruserdirectory/Pictures/BvM_graph.png");

        // Draw and save DeltaBeta vs Momentum histogram
        ec2.draw(DBvM);
        ec2.save("/home/youruserdirectory/Pictures/DBvM_graph.png");

        // Project DeltaBeta vs Momentum histogram onto Y axis (deltaBeta)
        H1F projectionY = DBvM.projectionY();
        TCanvas canvasProjection = new TCanvas("Projection of Y", 600, 600);
        canvasProjection.draw(projectionY);
        projectionY.setTitleX("#Delta#beta");
        canvasProjection.draw(projectionY);
        projectionY.setFillColor(4);
        projectionY.setLineColor(2);
        projectionY.setTitleY("Counts");
        canvasProjection.draw(projectionY);
        projectionY.save("/home/youruserdirectory/Pictures/ProjectionY_graph.png");

        // Fit slices of DeltaBeta vs Momentum histogram to Gaussians
        ParallelSliceFitter fitter = new ParallelSliceFitter(DBvM);
        fitter.setRange(-0.033, 0.033); // Fit range for deltaBeta
        fitter.setBackgroundOrder(ParallelSliceFitter.P1_BG); // Linear background
        fitter.fitSlicesX(); // Perform fits for each momentum bin
        fitter.getInspectFitsPane();
        fitter.inspectFits();

        // Create graphs for Gaussian mean and width vs momentum
        GraphErrors meanVsMomentum = new GraphErrors("Gaussian Mean vs Momentum");
        GraphErrors widthVsMomentum = new GraphErrors("Gaussian Width vs Momentum");

        // Extract mean and width from fits for each momentum bin
        for (int i = 0; i < fitter.getMeanSlices().getDataSize(0); i++) {
            // Get the center momentum value for this bin
            double momentum = fitter.getMeanSlices().getDataX(i);
            // Get the fitted mean (center) of the Gaussian for this bin
            double mean = fitter.getMeanSlices().getDataY(i);
            // Get the error on the mean
            double meanError = fitter.getMeanSlices().getDataEY(i);
            // Get the fitted width (sigma) of the Gaussian for this bin
            double width = fitter.getSigmaSlices().getDataY(i);
            // Get the error on the width
            double widthError = fitter.getSigmaSlices().getDataEY(i);

            // Add the mean and its error to the mean vs momentum graph
            meanVsMomentum.addPoint(momentum, mean, 0, meanError);
            // Add the width and its error to the width vs momentum graph
            widthVsMomentum.addPoint(momentum, width, 0, widthError);
        }

        // Draw and save mean vs momentum graph
        TCanvas meanCanvas = new TCanvas("Mean vs Momentum", 600, 600);
        meanCanvas.draw(meanVsMomentum);
        meanCanvas.getPad().getAxisY().setRange(-0.0015, 0.0007); // Set Y-axis range for deltaBeta
        meanCanvas.save("/home/youruserdirectory/Pictures/Gaussian_Mean_vs_Momentum.png");

        // Draw and save width vs momentum graph
        TCanvas widthCanvas = new TCanvas("Width(Sigma) vs Momentum", 600, 600);
        widthCanvas.draw(widthVsMomentum);
        widthCanvas.getPad().getAxisY().setRange(-0.025, 0.015); // Set Y-axis range for width
        widthCanvas.save("/home/youruserdirectory/Pictures/Gaussian_Width_vs_Momentum.png");

        // Print summary statistics
        System.out.println("Total Events Processed: " + eventCount);
        System.out.println("Events with Electron and Proton: " + protonCount);
        System.out.println("Total Protons in DeltaBeta vs Momentum: " + DBvM.getEntries());

        // Count protons within 1 standard deviation of the mean deltaBeta for each momentum bin
        int countProtons = 0;
        for (int ix = 0; ix < DBvM.getXAxis().getNBins(); ix++) {
            // Get the mean and standard deviation (width) for this momentum bin from the fit results
            double mean = meanVsMomentum.getDataY(ix);
            double stdDev = widthVsMomentum.getDataY(ix);
            for (int iy = 0; iy < DBvM.getYAxis().getNBins(); iy++) {
                // Get the center value of the deltaBeta bin
                double deltaBeta = DBvM.getYAxis().getBinCenter(iy);
                // Check if this deltaBeta is within 1 sigma of the mean for this momentum bin
                // (can change the multiplier for a wider window, e.g., 2 * stdDev for 2 sigma)
                if (Math.abs(deltaBeta - mean) <= stdDev) {
                    // Add the number of protons in this bin to the total count
                    countProtons += (int) DBvM.getBinContent(ix, iy);
                }
            }
        }
        System.out.println("Count of Protons within 1 Standard Deviation: " + countProtons);
        //System.out.println("Count of Protons within 2 Standard Deviations: " + countProtons);
    }
}
