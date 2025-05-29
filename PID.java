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

        HipoChain reader = new HipoChain();

        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00040.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00041.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00042.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00043.hipo");
        reader.open();

        Event event = new Event();
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // Histogram for Beta vs Momentum
        H2F BvM = new H2F("hBetaVsPTot", 100, 0.0, 6.0, 100, 0.1, 1.05);
        BvM.setTitleX("P [GeV]");
        BvM.setTitleY("#beta");

        // Histogram for DeltaBeta vs Momentum
        H2F DBvM = new H2F("DBvM", 25, 0.60, 3.0, 100, -0.033, 0.033);
        DBvM.setTitleX("P [GeV]");
        DBvM.setTitleY("D#beta");

        Vector3 v3Parts = new Vector3(0.0, 0.0, 0.0);

        TCanvas ec = new TCanvas("BvM", 600, 600);
        TCanvas ec2 = new TCanvas("DBvM", 600, 600);

        int protonCount = 0;
        int eventCount = 0;

        // Mass of proton in GeV
        float mass = 0.938f;

        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);

            boolean hasElectron = false;
            boolean hasProton = false;

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

                if (hasElectron && hasProton) {
                    for (int i = 0; i < particles.getRows(); i++) {
                        int pid = particles.getInt("pid", i);
                        int charge = particles.getInt("charge", i);

                        if (particles.getFloat("beta", i) > 0) {
                            v3Parts.setXYZ(
                                    particles.getFloat("px", i),
                                    particles.getFloat("py", i),
                                    particles.getFloat("pz", i));
                            float momentum = (float) v3Parts.mag();
                            float observedBeta = particles.getFloat("beta", i);

                            // Fill Beta vs Momentum histogram
                            BvM.fill(momentum, observedBeta);

                            if (pid == 2212) {
                                // Calculate expected beta and deltaBeta
                                float expectedBeta = momentum / (float) Math.sqrt(Math.pow(momentum, 2) + Math.pow(mass, 2));
                                float deltaBeta = observedBeta - expectedBeta;

                                // Fill DeltaBeta vs Momentum histogram
                                if (deltaBeta >= -0.03 && deltaBeta <= 0.03) {
                                    DBvM.fill(momentum, deltaBeta);
                                }
                            }
                        }
                    }
                    protonCount++;
                }
                eventCount++;
            }
        }

        // Draw and save histograms
        ec.draw(BvM);
        ec.save("/home/reh1/Pictures/BvM_graph.png");

        ec2.draw(DBvM);
        ec2.save("/home/reh1/Pictures/DBvM_graph.png");

        // Projection of DeltaBeta vs Momentum
        H1F projectionY = DBvM.projectionY();
        TCanvas canvasProjection = new TCanvas("Projection of Y", 600, 600);
        canvasProjection.draw(projectionY);
        projectionY.setTitleX("#Delta#beta");
        canvasProjection.draw(projectionY);
        projectionY.setFillColor(4);
        projectionY.setLineColor(2);
        projectionY.setTitleY("Counts");
        canvasProjection.draw(projectionY);
        projectionY.save("/home/reh1/Pictures/ProjectionY_graph.png");


        // Fit slices and create graphs for Gaussian mean and width
        ParallelSliceFitter fitter = new ParallelSliceFitter(DBvM);
        fitter.setRange(-0.033, 0.033); // Set the range for fitting
        fitter.setBackgroundOrder(ParallelSliceFitter.P1_BG); // Set background order
        fitter.fitSlicesX(); // Perform the slice fitting
        fitter.getInspectFitsPane();
        fitter.inspectFits();

// Create graphs for Gaussian mean and width
        GraphErrors meanVsMomentum = new GraphErrors("Gaussian Mean vs Momentum");
        GraphErrors widthVsMomentum = new GraphErrors("Gaussian Width vs Momentum");

// Extract data from the fitted slices
        for (int i = 0; i < fitter.getMeanSlices().getDataSize(0); i++) {
            double momentum = fitter.getMeanSlices().getDataX(i);
            double mean = fitter.getMeanSlices().getDataY(i);
            double meanError = fitter.getMeanSlices().getDataEY(i);
            double width = fitter.getSigmaSlices().getDataY(i);
            double widthError = fitter.getSigmaSlices().getDataEY(i);

            meanVsMomentum.addPoint(momentum, mean, 0, meanError);
            widthVsMomentum.addPoint(momentum, width, 0, widthError);
        }

// Draw and save the graphs
        TCanvas meanCanvas = new TCanvas("Mean vs Momentum", 600, 600);
        meanCanvas.draw(meanVsMomentum);
        meanCanvas.save("/home/reh1/Pictures/Gaussian_Mean_vs_Momentum.png");

        TCanvas widthCanvas = new TCanvas("Width vs Momentum", 600, 600);
        widthCanvas.draw(widthVsMomentum);
        widthCanvas.save("/home/reh1/Pictures/Gaussian_Width_vs_Momentum.png");

        System.out.println("Total Events Processed: " + eventCount);
        System.out.println("Events with Electron and Proton: " + protonCount);
    }
}
