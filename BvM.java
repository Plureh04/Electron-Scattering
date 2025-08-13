
import org.jlab.groot.fitter.ParallelSliceFitter;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.groot.data.*;
import org.jlab.groot.ui.*;
import org.jlab.clas.physics.*;
import org.jlab.groot.data.H1F;
import java.util.ArrayList;

public class BvM {

    public static void main(String[] args) {

        HipoChain reader = new HipoChain();

        reader.addFile("/home/userdirectory/Downloads/rec_clas_020508.evio.00040.hipo");
        reader.addFile("/home/userdirectory/Downloads/rec_clas_020508.evio.00041.hipo");
        reader.addFile("/home/userdirectory/Downloads/rec_clas_020508.evio.00042.hipo");
        reader.addFile("/home/userdirectory/Downloads/rec_clas_020508.evio.00043.hipo");
        reader.open();
        Event event = new Event();
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));
        /**
         * Create the histogram object and canvas
         * Make a name and initialize the Vector3 object
         */
        H2F BvM = new H2F("hBetaVsPTot", 100, 0.0, 6.0, 100, 0.1, 1.05);
        BvM.setTitleX("P [GeV]");
        BvM.setTitleY("#beta");

        Vector3 v3Parts = new Vector3(0.0, 0.0, 0.0);

        TCanvas ec = new TCanvas("BvM", 800, 800);
        //initialize the counter
        int proton = 0;
        int event_proton = 0;
        //loop over the events, checking for protons
        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);
            if (particles.getRows() > 0) {
                event_proton = 0;
                for (int i = 0; i < particles.getRows(); i++) {
                    int pid = particles.getInt("pid", i);
                    int charge = particles.getInt("charge", i);
                    if (particles.getFloat("beta", i) > 0) {
                        //id of proton is 2212
                        //if (pid == 2212) {
                        if (charge > 0) {
                            //Get beta and momentum of the particle, I think idk
                            //Momentum = p = sqrt(px^2 + py^2 + pz^2)
                            v3Parts.setXYZ(
                                    particles.getFloat("px", i),
                                    particles.getFloat("py", i),
                                    particles.getFloat("pz", i));
                            float momentum = (float) v3Parts.mag();
                            BvM.fill(momentum, particles.getFloat("beta", i));
                            event_proton++;

                        }
                    }
                }
                if (event_proton >= 1) {
                    proton = proton + event_proton;
                }
            }
            /**
             * Create another graph for deltaBeta and P
             * deltaBeta = Beta(data) - Beta(calculated)
             * Beta = p/sqrt(p^2 + m^2)
             * m = 0.93827 GeV
             * DB = deltaBeta
             * M = Momentum
             */
            // Mass of proton in GeV
            float mass = 0.938f;

// Create another graph for deltaBeta and P
            H2F DBvM = new H2F("DBvM", 50, 0.50, 8.0, 100, -0.055, 0.055);
            DBvM.setTitleX("P [GeV]");
            DBvM.setTitleY("D#beta");
            ArrayList<H1F> Slice1 = DBvM.getSlicesX();
            TCanvas ec2 = new TCanvas("DBvM", 800, 800);
// Loop over the events, checking for protons
            while (reader.hasNext()) {
                reader.nextEvent(event);
                event.read(particles);
                if (particles.getRows() > 0) {
                    event_proton = 0;
                    for (int i = 0; i < particles.getRows(); i++) {
                        int pid = particles.getInt("pid", i);
                        int charge = particles.getInt("charge", i);
                        if (particles.getFloat("beta", i) > 0) {
                            //id of proton is 2212
                            //if (pid == 2212) {
                            if (charge > 0) {
                                // Get beta and momentum of the particle
                                v3Parts.setXYZ(
                                        particles.getFloat("px", i),
                                        particles.getFloat("py", i),
                                        particles.getFloat("pz", i));
                                float momentum = (float) v3Parts.mag();
                                float observedBeta = particles.getFloat("beta", i);
                                BvM.fill(momentum, observedBeta);
                                event_proton++;

                                // Calculate expected beta and deltaBeta
                                float expectedBeta = momentum / (float) Math.sqrt(Math.pow(momentum, 2) + Math.pow(mass, 2));
                                float deltaBeta = observedBeta - expectedBeta;

                                // Check if deltaBeta is within the desired range before filling the DBvM graph
                                if (deltaBeta >= -0.05 && deltaBeta <= 0.05) {
                                    DBvM.fill(momentum, deltaBeta);
                                }
                            }
                        }
                    }
                    if (event_proton >= 1) {
                        proton = proton + event_proton;
                    }
                }
            }
            /**
             * Make slices in momentum
             * Fit the slices with a Gaussian function
             * Gaussian Function: f(x) = ae^(-((x-b)^2)/(2c^2))
             * a = amplitude
             * b = mean
             * c = standard deviation
             * x = momentum
             * e = Euler's number = 2.71828
             */
            ec.draw(BvM);
            ec.save("/home/youruserdirectory/Pictures/BvM_graph.png");
            ec2.draw(DBvM);
            ec2.save("/home/youruserdirectory/Pictures/DBvM_graph.png");

            // Assuming the projectY method exists and works as described
            H1F projectionY = DBvM.projectionY();
            TCanvas canvasProjection = new TCanvas("Projection of Y", 800, 800);
            canvasProjection.draw(projectionY);
            projectionY.setTitleX("#Delta#beta");
            canvasProjection.draw(projectionY);
            projectionY.setFillColor(4);
            projectionY.setLineColor(2);
            projectionY.setTitleY("Counts");
            canvasProjection.draw(projectionY);



            // create the canvas
            TCanvas can = new TCanvas("can",600,600);

            // Declare the slicer with the H2F histogram as the input
            ParallelSliceFitter fitter = new ParallelSliceFitter(DBvM);

//Set the range of the fits in the sliced H1F histograms
            fitter.setRange(-0.055,0.055); // Adjust the range as necessary for your data

// Fit is a Gaussian. This line adds a 1st order polynomial background to the fit
            fitter.setBackgroundOrder(ParallelSliceFitter.P1_BG);

// Slice and fit the projected histograms
            fitter.fitSlicesX(); // Assuming fitSlicesX() slices along the X-axis and fits each slice
            // draw the mean values for the fits of the sliced histograms
            can.draw(fitter.getMeanSlices());

// draw the sliced histograms with each fit.
            fitter.getInspectFitsPane();
            fitter.inspectFits();
            can.save("/home/youruserdirectory/Pictures/Can_graph.png");
           // fitter.save("/home/userdirectory/Pictures/Fitter_graph.png");


        }
    }
}



