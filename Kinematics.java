// src/Kinematics.java
import org.jlab.groot.ui.TCanvas;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.groot.data.*;
import org.jlab.clas.physics.*;

public class Kinematics {
    public static void main(String[] args) {
        long st = System.currentTimeMillis(); // Start time for performance measurement
        // Create a HipoChain reader and add HIPO files to process
        HipoChain reader = new HipoChain();
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00040.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00041.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00042.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00043.hipo");
        reader.open();

        // Create an Event object to hold each event and a Bank for particle data
        Event event = new Event();
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // Define histograms for DIS kinematics variables
        // W: Invariant mass of the hadronic system
        H1F hW = new H1F("hW", 100, 0.5, 4.5);
        hW.setTitle("W Distribution");
        hW.setTitleX("W [GeV]");
        hW.setTitleY("Counts");
        hW.setFillColor(38);

        // Q^2: Squared four-momentum transfer
        H1F hQ2 = new H1F("hQ2", 100, 0.5, 6.0);
        hQ2.setTitle("Q^2 Distribution");
        hQ2.setTitleX("Q^2 [GeV^2]");
        hQ2.setTitleY("Counts");
        hQ2.setFillColor(4);
        hQ2.setLineColor(1);

        // nu: Energy transfer
        H1F hNu = new H1F("hNu", 100, 0, 10.0);
        hNu.setTitle("Nu Distribution (energy transfer)");
        hNu.setTitleX("nu [GeV]");
        hNu.setTitleY("Counts");
        hNu.setFillColor(42);

        // 2D histogram: Q^2 vs W
        H2F hQ2vW = new H2F("hQ2vW", 100, 0.5, 5.0, 100, 0.5, 4.0);
        hQ2vW.setTitle("Q^2 vs W");
        hQ2vW.setTitleX("W [GeV]");
        hQ2vW.setTitleY("Q^2 [GeV^2]");

        // y: Inelasticity
        H1F hY = new H1F("hY", 100, 0, 1.0);
        hY.setTitle("Y distribution");
        hY.setTitleX("y");
        hY.setTitleY("Counts");
        hY.setFillColor(44);

        // 2d Histogram for Bjorken X
        H2F hXvQ2 = new H2F("hXvQ2", 100, 0.0, 1.0, 100, 0.5, 6.0);
        hXvQ2.setTitle("Bjorken X");
        hXvQ2.setTitleX("Bjorken X");
        hXvQ2.setTitleY("Q^2 [GeV^2]");

        // 2d Histogram for Bjorken Z
        H2F hZvQ2 = new H2F("hZvQ2", 100, 0.0, 1.0, 100, 0.5, 6.0);
        hZvQ2.setTitle("Bjorken Z");
        hZvQ2.setTitleX("Bjorken Z");
        hZvQ2.setTitleY("Q^2 [GeV^2]");

        // Define Lorentz vectors for the beam, target, and electron
        // Beam: 10.6 GeV along z, Target: proton at rest
        LorentzVector vBeam = new LorentzVector(0.0, 0.0, 10.6, 10.6);
        LorentzVector vTarget = new LorentzVector(0.0, 0.0, 0.0, 0.938);
        LorentzVector electron = new LorentzVector();
        LorentzVector vW = new LorentzVector();
        LorentzVector vQ2 = new LorentzVector();

        // Loop over all events in the HIPO files
        while (reader.hasNext()) {
            reader.nextEvent(event);      // Read next event
            event.read(particles);        // Read particle bank for this event

            // Loop over all particles in the event
            for (int i = 0; i < particles.getRows(); i++) {
                int pid = particles.getInt("pid", i); // Get particle ID
                if (pid == 11) { // Select electrons only (pid 11)
                    // Get electron momentum components
                    float px = particles.getFloat("px", i);
                    float py = particles.getFloat("py", i);
                    float pz = particles.getFloat("pz", i);

                    // Set electron Lorentz vector (mass = 0.0005 GeV)
                    electron.setPxPyPzM(px, py, pz, 0.0005);

                    // Calculate W: (beam + target - electron)
                    vW.copy(vBeam);
                    vW.add(vTarget);
                    vW.sub(electron);

                    // Calculate Q^2: (beam - electron)
                    vQ2.copy(vBeam);
                    vQ2.sub(electron);

                    // Extract kinematic variables
                    double Q2 = -vQ2.mass2();           // Q^2 (GeV^2)
                    double W = vW.mass();               // W (GeV)
                    double nu = vBeam.e() - electron.e(); // Energy transfer (GeV)
                    double y = nu / vBeam.e();          // Inelasticity
                    double x = Q2 / (2 * 0.938 * nu);   // Bjorken X
                    double z = vW.e() / vBeam.e();       // Bjorken Z

                    // Apply DIS cuts: Q^2 > 1 GeV^2, W > 2 GeV, y < 0.85
                    if (Q2 > 1.0 && W > 2.0 && y < 0.85) {
                        hW.fill(W);           // Fill W histogram
                        hQ2.fill(Q2);         // Fill Q^2 histogram
                        hQ2vW.fill(W, Q2);    // Fill 2D Q^2 vs W histogram
                        hNu.fill(nu);         // Fill nu histogram
                        hY.fill(y);           // Fill y histogram
                        hXvQ2.fill(x, Q2);    // Fill Bjorken X vs Q^2 histogram
                        hZvQ2.fill(z, Q2);    // Fill Bjorken Z vs Q^2 histogram
                    }
                }
            }
        }

        // Create a canvas, draw, and save all histograms
        TCanvas kinCanvas = new TCanvas("DIS Kinematics", 1500, 1000);
        kinCanvas.divide(3, 3);           // Divide canvas into 3x3 pads
        kinCanvas.cd(0).draw(hQ2);        // Draw Q^2
        kinCanvas.cd(1).draw(hW);         // Draw W
        kinCanvas.cd(2).draw(hNu);        // Draw nu
        kinCanvas.cd(4).draw(hQ2vW);      // Draw Q^2 vs W
        kinCanvas.cd(3).draw(hY);         // Draw y
        kinCanvas.cd(5).draw(hXvQ2);      // Draw Bjorken X vs Q^2
        kinCanvas.cd(6).draw(hZvQ2);      // Draw Bjorken Z vs Q^2
        kinCanvas.save("/home/reh1/Pictures/DIS_Kinematics.png"); // Save canvas as PNG

        System.out.println("Done processing DIS kinematics.");

        long et = System.currentTimeMillis(); // End time for performance measurement
        long time = et - st; // Calculate elapsed time
        System.out.println(" Time = " + (time / 1000.0) + " seconds"); // Print elapsed time
    }
}
