// src/Final_ratio.java

import org.jlab.groot.ui.TCanvas;
import org.jlab.groot.data.*;
import org.jlab.clas.physics.*;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;

/**
 * Calculates and plots multiplicity ratios for Carbon and Deuterium targets.
 * - Q2, Nu: bin-by-bin multiplicity ratio (proton/electron, C/D)
 * - Zh, Pt^2: proton ratio histogram divided by total electron ratio (single number)
 */
public class Final_ratio {
    public static void main(String[] args) {
        // Initialize HIPO reader and particle bank
        HipoChain reader = Carbon_F.getCarbonChain();
        Event event = new Event();
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // Histograms for electrons (Carbon and Deuterium)
        H1F hQ2_C_e = new H1F("hQ2_C_e", 50, 0, 10);
        H1F hQ2_D_e = new H1F("hQ2_D_e", 50, 0, 10);
        H1F hNu_C_e = new H1F("hNu_C_e", 50, 0, 10);
        H1F hNu_D_e = new H1F("hNu_D_e", 50, 0, 10);
        H1F hZ_C_e = new H1F("hZ_C_e", 50, 0, 1.2);
        H1F hZ_D_e = new H1F("hZ_D_e", 50, 0, 1.2);
        H1F hPt2_C_e = new H1F("hPt2_C_e", 50, 0, 2.0);
        H1F hPt2_D_e = new H1F("hPt2_D_e", 50, 0, 2.0);

        // Histograms for protons (Carbon and Deuterium)
        H1F hQ2_C_p = new H1F("hQ2_C_p", 50, 0, 10);
        H1F hQ2_D_p = new H1F("hQ2_D_p", 50, 0, 10);
        H1F hNu_C_p = new H1F("hNu_C_p", 50, 0, 10);
        H1F hNu_D_p = new H1F("hNu_D_p", 50, 0, 10);
        H1F hZ_C_p = new H1F("hZ_C_p", 50, 0, 1.2);
        H1F hZ_D_p = new H1F("hZ_D_p", 50, 0, 1.2);
        H1F hPt2_C_p = new H1F("hPt2_C_p", 50, 0, 2.0);
        H1F hPt2_D_p = new H1F("hPt2_D_p", 50, 0, 2.0);

        // Event counters for normalization
        int count_C_e = 0, count_D_e = 0, count_C_p = 0, count_D_p = 0;

        // Beam and electron Lorentz vectors
        LorentzVector vBeam = new LorentzVector(0.0, 0.0, 10.5473, 10.5473);
        LorentzVector electron = new LorentzVector();

        // Event loop: read all events from the HIPO file
        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);

            // Find the first electron in the event
            int electronIndex = -1;
            for (int i = 0; i < particles.getRows(); i++) {
                if (particles.getInt("pid", i) == 11) {
                    electronIndex = i;
                    break;
                }
            }
            if (electronIndex == -1) continue; // Skip if no electron

            // Get electron kinematics
            float e_px = particles.getFloat("px", electronIndex);
            float e_py = particles.getFloat("py", electronIndex);
            float e_pz = particles.getFloat("pz", electronIndex);
            electron.setPxPyPzM(e_px, e_py, e_pz, 0.000511);

            // Calculate DIS variables
            LorentzVector vVirtualPhoton = new LorentzVector();
            vVirtualPhoton.copy(vBeam);
            vVirtualPhoton.sub(electron);
            double Q2 = -vVirtualPhoton.mass2();
            double nu = vBeam.e() - electron.e();
            double W2 = 0.938 * 0.938 + 2 * 0.938 * nu - Q2;
            double W = Math.sqrt(W2);
            double y = nu / vBeam.e();

            // Apply DIS cuts
            if (Q2 <= 1.0 || W <= 2.0 || y >= 0.85) continue;

            float e_vz = particles.getFloat("vz", electronIndex);
            double z_e = nu > 0 ? electron.e() / nu : 0;
            double pt2_e = e_px * e_px + e_py * e_py;

            // Fill electron histograms and increment counters
            if (e_vz > -8 && e_vz < -2) { // Deuterium
                hQ2_D_e.fill(Q2);
                hNu_D_e.fill(nu);
                hZ_D_e.fill(z_e);
                hPt2_D_e.fill(pt2_e);
                count_D_e++;
            } else { // Carbon
                hQ2_C_e.fill(Q2);
                hNu_C_e.fill(nu);
                hZ_C_e.fill(z_e);
                hPt2_C_e.fill(pt2_e);
                count_C_e++;
            }

            // Loop over all protons in the event
            for (int i = 0; i < particles.getRows(); i++) {
                if (particles.getInt("pid", i) == 2212) {
                    float p_px = particles.getFloat("px", i);
                    float p_py = particles.getFloat("py", i);
                    float p_pz = particles.getFloat("pz", i);
                    float p_vz = particles.getFloat("vz", i);

                    LorentzVector proton = new LorentzVector();
                    proton.setPxPyPzM(p_px, p_py, p_pz, 0.938);
                    double z_p = nu > 0 ? proton.e() / nu : 0;
                    double pt2_p = p_px * p_px + p_py * p_py;

                    // Fill proton histograms and increment counters
                    if (p_vz > -8 && p_vz < -2) { // Deuterium
                        hQ2_D_p.fill(Q2);
                        hNu_D_p.fill(nu);
                        hZ_D_p.fill(z_p);
                        hPt2_D_p.fill(pt2_p);
                        count_D_p++;
                    } else { // Carbon
                        hQ2_C_p.fill(Q2);
                        hNu_C_p.fill(nu);
                        hZ_C_p.fill(z_p);
                        hPt2_C_p.fill(pt2_p);
                        count_C_p++;
                    }
                }
            }
        }

        // Compute C/D ratios for electrons and protons (bin-by-bin)
        H1F hRatioQ2_e = hQ2_C_e.histClone("hRatioQ2_e"); hRatioQ2_e.divide(hQ2_D_e);
        H1F hRatioNu_e = hNu_C_e.histClone("hRatioNu_e"); hRatioNu_e.divide(hNu_D_e);

        H1F hRatioQ2_p = hQ2_C_p.histClone("hRatioQ2_p"); hRatioQ2_p.divide(hQ2_D_p);
        H1F hRatioNu_p = hNu_C_p.histClone("hRatioNu_p"); hRatioNu_p.divide(hNu_D_p);
        H1F hRatioZh_p = hZ_C_p.histClone("hRatioZh_p"); hRatioZh_p.divide(hZ_D_p);
        H1F hRatioPt2_p = hPt2_C_p.histClone("hRatioPt2_p"); hRatioPt2_p.divide(hPt2_D_p);

        // Total electron ratio for Zh and Pt2 (single number)
        double totalElectronRatio = (double) count_C_e / count_D_e;

        // Multiplicity ratio for Q2 and Nu: (proton ratio) / (electron ratio), bin-by-bin
        H1F hMultRatioQ2 = hRatioQ2_p.histClone("hMultRatioQ2");
        hMultRatioQ2.divide(hRatioQ2_e);

        H1F hMultRatioNu = hRatioNu_p.histClone("hMultRatioNu");
        hMultRatioNu.divide(hRatioNu_e);

        // Multiplicity ratio for Zh and Pt2: divide each bin by total electron ratio
        H1F hMultRatioZh = hRatioZh_p.histClone("hMultRatioZh");
        for (int i = 0; i < hMultRatioZh.getAxis().getNBins(); i++) {
            double binVal = hMultRatioZh.getBinContent(i);
            hMultRatioZh.setBinContent(i, binVal / totalElectronRatio);
        }

        H1F hMultRatioPt2 = hRatioPt2_p.histClone("hMultRatioPt2");
        for (int i = 0; i < hMultRatioPt2.getAxis().getNBins(); i++) {
            double binVal = hMultRatioPt2.getBinContent(i);
            hMultRatioPt2.setBinContent(i, binVal / totalElectronRatio);
        }

        // Plot and save multiplicity ratio graphs
        TCanvas cMultQ2 = new TCanvas("Multiplicity Ratio Q2", 800, 600);
        hMultRatioQ2.setFillColor(3);
        // Set labels for the x and y axes
        hMultRatioQ2.setTitle("Multiplicity Ratio C/D vs Q^{2}");
        hMultRatioQ2.setTitleX("Q^{2} [GeV^{2}]");
        hMultRatioQ2.setTitleY("C/D Ratio");
        cMultQ2.draw(hMultRatioQ2);
        cMultQ2.save("/home/youruserdirectory/Pictures/Carbon/Mult.Ratios/MultiplicityRatio_Q2.png");

        TCanvas cMultNu = new TCanvas("Multiplicity Ratio Nu", 800, 600);
        hMultRatioNu.setFillColor(2);
        hMultRatioNu.setTitle("Multiplicity Ratio C/D vs Nu");
        hMultRatioNu.setTitleX("Nu [GeV]");
        hMultRatioNu.setTitleY("C/D Ratio");
        cMultNu.draw(hMultRatioNu);
        cMultNu.save("/home/youruserdirectory/Pictures/Carbon/Mult.Ratios/MultiplicityRatio_Nu.png");

        TCanvas cMultZh = new TCanvas("Multiplicity Ratio Zh", 800, 600);
        hMultRatioZh.setFillColor(3);
        hMultRatioZh.setTitle("Multiplicity Ratio C/D vs Z_{h}");
        hMultRatioZh.setTitleX("Z_{h}");
        hMultRatioZh.setTitleY("C/D Ratio");
        cMultZh.draw(hMultRatioZh);
        cMultZh.save("/home/youruserdirectory/Pictures/Carbon/Mult.Ratios/MultiplicityRatio_Zh.png");

        TCanvas cMultPt2 = new TCanvas("Multiplicity Ratio Pt2", 800, 600);
        hMultRatioPt2.setFillColor(5);
        hMultRatioPt2.setTitle("Multiplicity Ratio C/D vs P_{t}^{2}");
        hMultRatioPt2.setTitleX("P_{t}^{2} [GeV^{2}]");
        hMultRatioPt2.setTitleY("C/D Ratio");
        cMultPt2.draw(hMultRatioPt2);
        cMultPt2.save("/home/youruserdirectory/Pictures/Carbon/Mult.Ratios/MultiplicityRatio_Pt2.png");

        // Print event counts for reference
        System.out.println("Electron counts: Carbon = " + count_C_e + ", Deuterium = " + count_D_e);
        System.out.println("Proton counts: Carbon = " + count_C_p + ", Deuterium = " + count_D_p);
    }
}
