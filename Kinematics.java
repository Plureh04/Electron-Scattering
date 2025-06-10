// Import necessary classes for data handling, plotting, and physics calculations
import org.jlab.groot.ui.TCanvas;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.groot.data.*;
import org.jlab.clas.physics.*;

public class Kinematics extends PID {
    public static void main(String[] args) {
        // Create a HipoChain to read multiple HIPO files (CLAS12 data format)
        HipoChain reader = new HipoChain();

        // Add input data files to the reader
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00040.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00041.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00042.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00043.hipo");
        reader.open();

        // Create event and bank objects for reading particle data
        Event event = new Event();
        Bank gen = new Bank(reader.getSchemaFactory().getSchema("MC::Particle"));
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // --- Histogram creation and labeling/coloring ---

        // Histograms for electron kinematics (DIS variables)
        H1F hW = new H1F("hW", 100, 0.5, 4.5); // W invariant mass
        hW.setTitle("W Distribution");
        hW.setTitleX("W [GeV]");
        hW.setTitleY("Counts");
        hW.setFillColor(2);
        hW.setLineColor(1);

        H1F hQ2 = new H1F("hQ2", 100, 0.5, 6.0); // Q^2 momentum transfer
        hQ2.setTitle("Q^2 Distribution");
        hQ2.setTitleX("Q^2 [GeV^2]");
        hQ2.setTitleY("Counts");
        hQ2.setFillColor(4);
        hQ2.setLineColor(1);

        H1F hNu = new H1F("hNu", 100, 0, 10.0); // Energy transfer
        hNu.setTitle("Energy Transfer (nu)");
        hNu.setTitleX("nu [GeV]");
        hNu.setTitleY("Counts");
        hNu.setFillColor(3);
        hNu.setLineColor(1);

        H1F hY = new H1F("hY", 100, 0, 1.0); // Inelasticity
        hY.setTitle("Inelasticity (y)");
        hY.setTitleX("y");
        hY.setTitleY("Counts");
        hY.setFillColor(6);
        hY.setLineColor(1);

        H2F hQ2vW = new H2F("hQ2vW", 100, 0.5, 5.0, 100, 0.5, 4.0); // 2D Q^2 vs W
        hQ2vW.setTitle("Q^2 vs W");
        hQ2vW.setTitleX("W [GeV]");
        hQ2vW.setTitleY("Q^2 [GeV^2]");

        // Histograms for per-event electron variables
        H1F hElectronNum = new H1F("hElectronNum", 10, 0, 10); // Number of electrons per event
        hElectronNum.setTitle("Number of Electrons per Event");
        hElectronNum.setTitleX("N_e");
        hElectronNum.setTitleY("Counts");
        hElectronNum.setFillColor(4);
        hElectronNum.setLineColor(1);

        H1F hElectronTotP = new H1F("hElectronTotP", 100, 0, 5.0); // Total electron momentum per event
        hElectronTotP.setTitle("Total Electron Momentum per Event");
        hElectronTotP.setTitleX("Total p_e [GeV]");
        hElectronTotP.setTitleY("Counts");
        hElectronTotP.setFillColor(4);
        hElectronTotP.setLineColor(1);

        H1F hElectronPhi = new H1F("hElectronPhi", 100, -180, 180); // Electron azimuthal angle
        hElectronPhi.setTitle("Electron #phi");
        hElectronPhi.setTitleX("#phi [deg]");
        hElectronPhi.setTitleY("Counts");
        hElectronPhi.setFillColor(4);
        hElectronPhi.setLineColor(1);

        H1F hElectronTheta = new H1F("hElectronTheta", 100, 0, 60); // Electron polar angle
        hElectronTheta.setTitle("Electron #theta");
        hElectronTheta.setTitleX("#theta [deg]");
        hElectronTheta.setTitleY("Counts");
        hElectronTheta.setFillColor(4);
        hElectronTheta.setLineColor(1);

        H1F hElectronVz = new H1F("hElectronVz", 100, -15, 10); // Electron vertex z
        hElectronVz.setTitle("Electron v_z");
        hElectronVz.setTitleX("v_z [cm]");
        hElectronVz.setTitleY("Counts");
        hElectronVz.setFillColor(4);
        hElectronVz.setLineColor(1);

        // Histograms for per-event proton variables
        H1F hProtonNum = new H1F("hProtonNum", 10, 0, 10); // Number of protons per event
        hProtonNum.setTitle("Number of Protons per Event");
        hProtonNum.setTitleX("N_p");
        hProtonNum.setTitleY("Counts");
        hProtonNum.setFillColor(2);
        hProtonNum.setLineColor(1);

        H1F hProtonTotP = new H1F("hProtonTotP", 100, 0, 5.0); // Total proton momentum per event
        hProtonTotP.setTitle("Total Proton Momentum per Event");
        hProtonTotP.setTitleX("Total p_p [GeV]");
        hProtonTotP.setTitleY("Counts");
        hProtonTotP.setFillColor(2);
        hProtonTotP.setLineColor(1);

        H1F hProtonPhi = new H1F("hProtonPhi", 100, -180, 180); // Proton azimuthal angle
        hProtonPhi.setTitle("Proton #phi");
        hProtonPhi.setTitleX("#phi [deg]");
        hProtonPhi.setTitleY("Counts");
        hProtonPhi.setFillColor(2);
        hProtonPhi.setLineColor(1);

        H1F hProtonTheta = new H1F("hProtonTheta", 100, 0, 60); // Proton polar angle
        hProtonTheta.setTitle("Proton #theta");
        hProtonTheta.setTitleX("#theta [deg]");
        hProtonTheta.setTitleY("Counts");
        hProtonTheta.setFillColor(2);
        hProtonTheta.setLineColor(1);

        H1F hProtonVz = new H1F("hProtonVz", 100, -15, 10); // Proton vertex z
        hProtonVz.setTitle("Proton v_z");
        hProtonVz.setTitleX("v_z [cm]");
        hProtonVz.setTitleY("Counts");
        hProtonVz.setFillColor(2);
        hProtonVz.setLineColor(1);

        // Lorentz vectors for beam, target, and electron
        LorentzVector vBeam = new LorentzVector(0.0, 0.0, 10.6, 10.6); // Beam: 10.6 GeV along z
        LorentzVector vTarget = new LorentzVector(0.0, 0.0, 0.0, 0.938); // Target: proton at rest
        LorentzVector electron = new LorentzVector(); // Placeholder for each event's electron
        LorentzVector vW = new LorentzVector(); // For W calculation
        LorentzVector vQ2 = new LorentzVector(); // For Q^2 calculation

        // --- Event Loop: process all events in the input files ---
        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);

            // Per-event counters and accumulators
            int electronCount = 0;
            int protonCount = 0;
            double electronTotP = 0;
            double protonTotP = 0;

            // Lists to store per-event kinematic variables for electrons and protons
            java.util.ArrayList<Double> electronPhis = new java.util.ArrayList<>();
            java.util.ArrayList<Double> electronThetas = new java.util.ArrayList<>();
            java.util.ArrayList<Double> electronVzs = new java.util.ArrayList<>();
            java.util.ArrayList<Double> protonPhis = new java.util.ArrayList<>();
            java.util.ArrayList<Double> protonThetas = new java.util.ArrayList<>();
            java.util.ArrayList<Double> protonVzs = new java.util.ArrayList<>();

            // Loop over all particles in the event
            if (particles.getRows() > 0) {
                for (int i = 0; i < particles.getRows(); i++) {
                    int pid = particles.getInt("pid", i); // Particle ID
                    float px = particles.getFloat("px", i);
                    float py = particles.getFloat("py", i);
                    float pz = particles.getFloat("pz", i);
                    float vx = particles.getFloat("vx", i);
                    float vy = particles.getFloat("vy", i);
                    float vz = particles.getFloat("vz", i);
                    float momentum = (float) Math.sqrt(px * px + py * py + pz * pz);

                    if (pid == 11) { // Electron
                        electronCount++;
                        electronTotP += momentum;
                        electronVzs.add((double) vz);

                        // Calculate phi and theta for the electron
                        double phi = Math.toDegrees(Math.atan2(py, px));
                        double theta = Math.toDegrees(Math.acos(pz / Math.sqrt(px * px + py * py + pz * pz)));
                        electronPhis.add(phi);
                        electronThetas.add(theta);

                        // Set electron Lorentz vector (mass = 0.0005 GeV)
                        electron.setPxPyPzM(px, py, pz, 0.0005);

                        // Calculate W and Q^2 Lorentz vectors
                        vW.copy(vBeam);
                        vW.add(vTarget);
                        vW.sub(electron);

                        vQ2.copy(vBeam);
                        vQ2.sub(electron);

                        // Calculate DIS variables
                        double Q2 = -vQ2.mass2();
                        double W = vW.mass();
                        double nu = vBeam.e() - electron.e();
                        double y = nu / vBeam.e();

                        // Apply DIS cuts: Q2 > 1.0 GeV^2, W > 2 GeV, y < 0.85
                        if (Q2 > 1.0 && W > 2.0 && y < 0.85) {
                            hW.fill(W);
                            hQ2.fill(Q2);
                            hQ2vW.fill(W, Q2);
                            hNu.fill(nu);
                            hY.fill(y);
                        }
                    } else if (pid == 2212) { // Proton
                        protonCount++;
                        protonTotP += momentum;
                        protonVzs.add((double) vz);

                        // Calculate phi and theta for the proton
                        double phi = Math.toDegrees(Math.atan2(py, px));
                        double theta = Math.toDegrees(Math.acos(pz / Math.sqrt(px * px + py * py + pz * pz)));
                        protonPhis.add(phi);
                        protonThetas.add(theta);
                    }
                }
                // Fill per-event histograms for electrons
                hElectronNum.fill(electronCount);
                hElectronTotP.fill(electronTotP);
                for (double phi : electronPhis) hElectronPhi.fill(phi);
                for (double theta : electronThetas) hElectronTheta.fill(theta);
                for (double vz : electronVzs) hElectronVz.fill(vz);

                // Fill per-event histograms for protons
                hProtonNum.fill(protonCount);
                hProtonTotP.fill(protonTotP);
                for (double phi : protonPhis) hProtonPhi.fill(phi);
                for (double theta : protonThetas) hProtonTheta.fill(theta);
                for (double vz : protonVzs) hProtonVz.fill(vz);
            }
        }

        // --- Visualization: draw all histograms on canvases ---

        // Canvas for DIS kinematics (Q^2, W, nu, y, Q^2 vs W)
        TCanvas kinCanvas = new TCanvas("DIS Kinematics", 1200, 800);
        kinCanvas.divide(3, 2);
        kinCanvas.cd(0).draw(hQ2);
        kinCanvas.cd(1).draw(hW);
        kinCanvas.cd(2).draw(hNu);
        kinCanvas.cd(3).draw(hY);
        kinCanvas.cd(4).draw(hQ2vW);
        // Save the kinematics canvas to a file
        kinCanvas.save("/home/reh1/Pictures/DIS_Kinematics.png");

        // Canvas for electron variables
        TCanvas eCanvas = new TCanvas("Electron Variables", 1200, 800);
        eCanvas.divide(3, 2);
        eCanvas.cd(0).draw(hElectronNum);
        eCanvas.cd(1).draw(hElectronTotP);
        eCanvas.cd(2).draw(hElectronPhi);
        eCanvas.cd(3).draw(hElectronTheta);
        eCanvas.cd(4).draw(hElectronVz);
        // Save canvases to files
        eCanvas.save("/home/reh1/Pictures/DIS_Electron_Variables.png");

        // Canvas for proton variables
        TCanvas pCanvas = new TCanvas("Proton Variables", 1200, 800);
        pCanvas.divide(3, 2);
        pCanvas.cd(0).draw(hProtonNum);
        pCanvas.cd(1).draw(hProtonTotP);
        pCanvas.cd(2).draw(hProtonPhi);
        pCanvas.cd(3).draw(hProtonTheta);
        pCanvas.cd(4).draw(hProtonVz);
        // Save canvases to files
        pCanvas.save("/home/reh1/Pictures/DIS_Proton_Variables.png");
        // Print completion message
        System.out.println("Done processing events.");
    }
}
