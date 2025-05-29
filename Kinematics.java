import org.jlab.groot.ui.TCanvas;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.groot.data.*;
import org.jlab.clas.physics.*;

public class Kinematics {
    public static void main(String[] args) {
        HipoChain reader = new HipoChain();

        // Add files to the reader
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00040.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00041.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00042.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00043.hipo");
        reader.open();

        Event event = new Event();
        Bank gen = new Bank(reader.getSchemaFactory().getSchema("MC::Particle"));
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // Histograms for electron kinematics
        H1F hW = new H1F("hW", 100, 0.5, 5.0);
        H1F hQ2 = new H1F("hQ2", 100, 0.1, 6.0);
        hW.setTitleX("W [GeV]");
        hW.setFillColor(2);
        hQ2.setFillColor(4);
        hQ2.setTitleX("Q^2 [GeV/c^2]");

        H2F hBetaVsPTot = new H2F("hBetaVsPTot", 100, 0.25, 10.0, 100, 0.5, 1.05);
        hBetaVsPTot.setTitleX("P [GeV]");
        hBetaVsPTot.setTitleY("#beta");

        H2F hQ2vW = new H2F("hQ2vW", 100, 0.5, 5.0, 100, 0.5, 4.0);
        hQ2vW.setTitleX("W [GeV]");
        hQ2vW.setTitleY("Q^2 [GeV/c^2]");

        // Histograms for proton kinematics
        H1F hProtonMomentum = new H1F("hProtonMomentum", 100, 0, 5.0);
        H1F hProtonVx = new H1F("hProtonVx", 100, -5, 5);
        H1F hProtonVy = new H1F("hProtonVy", 100, -5, 5);
        H1F hProtonVz = new H1F("hProtonVz", 100, -20, 20);
        hProtonMomentum.setTitleX("Proton Momentum [GeV]");
        hProtonVx.setTitleX("Proton Vx [cm]");
        hProtonVy.setTitleX("Proton Vy [cm]");
        hProtonVz.setTitleX("Proton Vz [cm]");

        // Lorentz Vectors
        LorentzVector vBeam = new LorentzVector(0.0, 0.0, 10.6, 10.6);
        LorentzVector vTarget = new LorentzVector(0.0, 0.0, 0.0, 0.938);
        LorentzVector electron = new LorentzVector();
        LorentzVector vW = new LorentzVector();
        LorentzVector vQ2 = new LorentzVector();

        // Event Loop
        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);

            if (particles.getRows() > 0) {
                for (int i = 0; i < particles.getRows(); i++) {
                    int pid = particles.getInt("pid", i);
                    float px = particles.getFloat("px", i);
                    float py = particles.getFloat("py", i);
                    float pz = particles.getFloat("pz", i);
                    float vx = particles.getFloat("vx", i);
                    float vy = particles.getFloat("vy", i);
                    float vz = particles.getFloat("vz", i);
                    float momentum = (float) Math.sqrt(px * px + py * py + pz * pz);

                    if (particles.getFloat("beta", i) > 0) {
                        Vector3 v3Parts = new Vector3(px, py, pz);
                        hBetaVsPTot.fill(v3Parts.mag(), particles.getFloat("beta", i));
                    }

                    if (pid == 11) { // Electron
                        electron.setPxPyPzM(px, py, pz, 0.0005);

                        vW.copy(vBeam);
                        vW.add(vTarget);
                        vW.sub(electron);

                        vQ2.copy(vBeam);
                        vQ2.sub(electron);

                        hW.fill(vW.mass());
                        hQ2.fill(-vQ2.mass2());
                        hQ2vW.fill(vW.mass(), -vQ2.mass2());
                    } else if (pid == 2212) { // Proton
                        hProtonMomentum.fill(momentum);
                        hProtonVx.fill(vx);
                        hProtonVy.fill(vy);
                        hProtonVz.fill(vz);
                    }
                }
            }

            event.read(gen);
            if (gen.getRows() > 0) {
                for (int i = 0; i < gen.getRows(); i++) {
                    int pid = gen.getInt("pid", i);
                    if (pid == 11) { // Electron
                        electron.setPxPyPzM(
                                gen.getFloat("px", i),
                                gen.getFloat("py", i),
                                gen.getFloat("pz", i),
                                0.0005
                        );

                        vW.copy(vBeam);
                        vW.add(vTarget);
                        vW.sub(electron);

                        vQ2.copy(vBeam);
                        vQ2.sub(electron);

                        hW.fill(vW.mass());
                        hQ2.fill(-vQ2.mass2());
                        hQ2vW.fill(vW.mass(), -vQ2.mass2());
                    }
                }
            }
        }

        // Visualization
        TCanvas ec = new TCanvas("Electron Kinematics", 800, 800);
        ec.divide(2, 2);
        ec.cd(0).draw(hW);
        ec.cd(1).draw(hQ2);
        ec.cd(2).draw(hBetaVsPTot);
        ec.cd(3).draw(hQ2vW);

        TCanvas pc = new TCanvas("Proton Kinematics", 800, 800);
        pc.divide(2, 2);
        pc.cd(0).draw(hProtonMomentum);
        pc.cd(1).draw(hProtonVx);
        pc.cd(2).draw(hProtonVy);
        pc.cd(3).draw(hProtonVz);

        System.out.println("Done processing events.");
    }
}
