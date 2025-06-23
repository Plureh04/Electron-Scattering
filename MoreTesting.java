
import org.jlab.groot.ui.TCanvas;
import org.jlab.groot.math.F1D;
import org.jlab.jnp.hipo4.io.*;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.groot.data.*;
import org.jlab.clas.physics.*;

public class MoreTesting extends Testing {
    public static void main(String[] args) {
        HipoChain reader = new HipoChain();
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00040.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00041.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00042.hipo");
        reader.addFile("/home/reh1/Downloads/rec_clas_020508.evio.00043.hipo");
        reader.open();

        Event event = new Event();
        Bank particles = new Bank(reader.getSchemaFactory().getSchema("REC::Particle"));

        // --- Histograms for per-event electron variables ---
        H1F hElectronNum = new H1F("hElectronNum", 10, 0, 10);
        hElectronNum.setTitle("Number of Electrons per Event");
        hElectronNum.setTitleX("N_e");
        hElectronNum.setTitleY("Counts");
        hElectronNum.setFillColor(4);
        hElectronNum.setLineColor(1);

        H1F hElectronTotP = new H1F("hElectronTotP", 100, 0, 5.0);
        hElectronTotP.setTitle("Total Electron Momentum per Event");
        hElectronTotP.setTitleX("Total p_e [GeV]");
        hElectronTotP.setTitleY("Counts");
        hElectronTotP.setFillColor(4);
        hElectronTotP.setLineColor(1);

        H1F hElectronPhi = new H1F("hElectronPhi", 100, -180, 180);
        hElectronPhi.setTitle("Electron #phi");
        hElectronPhi.setTitleX("#phi [deg]");
        hElectronPhi.setTitleY("Counts");
        hElectronPhi.setFillColor(4);
        hElectronPhi.setLineColor(1);

        H1F hElectronTheta = new H1F("hElectronTheta", 100, 0, 60);
        hElectronTheta.setTitle("Electron #theta");
        hElectronTheta.setTitleX("#theta [deg]");
        hElectronTheta.setTitleY("Counts");
        hElectronTheta.setFillColor(4);
        hElectronTheta.setLineColor(1);

        H1F hElectronZ = new H1F("hElectronZ", 100, 0, 2);
        hElectronZ.setTitle("Electron Bjorken z");
        hElectronZ.setTitleX("z = E_e / nu");
        hElectronZ.setTitleY("Counts");
        hElectronZ.setFillColor(4);
        hElectronZ.setLineColor(1);

        // --- Histograms for per-event proton variables ---
        H1F hProtonNum = new H1F("hProtonNum", 10, 0, 10);
        hProtonNum.setTitle("Number of Protons per Event");
        hProtonNum.setTitleX("N_p");
        hProtonNum.setTitleY("Counts");
        hProtonNum.setFillColor(2);
        hProtonNum.setLineColor(1);

        H1F hProtonTotP = new H1F("hProtonTotP", 100, 0, 5.0);
        hProtonTotP.setTitle("Total Proton Momentum per Event");
        hProtonTotP.setTitleX("Total p_p [GeV]");
        hProtonTotP.setTitleY("Counts");
        hProtonTotP.setFillColor(2);
        hProtonTotP.setLineColor(1);

        H1F hProtonPhi = new H1F("hProtonPhi", 100, -180, 180);
        hProtonPhi.setTitle("Proton #phi");
        hProtonPhi.setTitleX("#phi [deg]");
        hProtonPhi.setTitleY("Counts");
        hProtonPhi.setFillColor(2);
        hProtonPhi.setLineColor(1);

        H1F hProtonTheta = new H1F("hProtonTheta", 100, 0, 60);
        hProtonTheta.setTitle("Proton #theta");
        hProtonTheta.setTitleX("#theta [deg]");
        hProtonTheta.setTitleY("Counts");
        hProtonTheta.setFillColor(2);
        hProtonTheta.setLineColor(1);

        H1F hProtonZ = new H1F("hProtonZ", 100, 0, 2);
        hProtonZ.setTitle("Proton Bjorken z");
        hProtonZ.setTitleX("z = E_p / nu");
        hProtonZ.setTitleY("Counts");
        hProtonZ.setFillColor(2);
        hProtonZ.setLineColor(1);

        // --- 2D Histograms: Number of protons vs Bjorken z for each target ---
        H2F hProtonNumVsZ_Deuterium = new H2F("hProtonNumVsZ_Deuterium", 100, 0, 2, 10, 0, 10);
        hProtonNumVsZ_Deuterium.setTitle("Number of Protons vs Bjorken z (Deuterium)");
        hProtonNumVsZ_Deuterium.setTitleX("Proton z");
        hProtonNumVsZ_Deuterium.setTitleY("Number of Protons");

        H2F hProtonNumVsZ_Carbon = new H2F("hProtonNumVsZ_Carbon", 100, 0, 2, 10, 0, 10);
        hProtonNumVsZ_Carbon.setTitle("Number of Protons vs Bjorken z (Carbon)");
        hProtonNumVsZ_Carbon.setTitleX("Proton z");
        hProtonNumVsZ_Carbon.setTitleY("Number of Protons");

        // --- Histogram for vz difference ---
        H1F hVzDiff = new H1F("hVzDiff", 100, -6, 4);
        hVzDiff.setTitle("Electron - Proton v_z Difference");
        hVzDiff.setTitleX("v {z,e} - v {z,p} [cm]");
        hVzDiff.setTitleY("Counts");
        hVzDiff.setFillColor(3);
        hVzDiff.setLineColor(1);

        // --- Lorentz vectors for beam and electron ---
        LorentzVector vBeam = new LorentzVector(0.0, 0.0, 10.5473, 10.5473);
        LorentzVector electron = new LorentzVector();

        while (reader.hasNext()) {
            reader.nextEvent(event);
            event.read(particles);

            int electronCount = 0;
            int protonCount = 0;
            double electronTotP = 0;
            double protonTotP = 0;

            java.util.ArrayList<Double> electronPhis = new java.util.ArrayList<>();
            java.util.ArrayList<Double> electronThetas = new java.util.ArrayList<>();
            java.util.ArrayList<double[]> electronMomenta = new java.util.ArrayList<>();
            java.util.ArrayList<Double> electronVzs = new java.util.ArrayList<>();
            java.util.ArrayList<Double> protonPhis = new java.util.ArrayList<>();
            java.util.ArrayList<Double> protonThetas = new java.util.ArrayList<>();
            java.util.ArrayList<double[]> protonMomenta = new java.util.ArrayList<>();
            java.util.ArrayList<Double> protonVzs = new java.util.ArrayList<>();

            boolean hasProtonInTarget = false;
            boolean hasProtonOutOfTarget = false;

            if (particles.getRows() > 0) {
                for (int i = 0; i < particles.getRows(); i++) {
                    int pid = particles.getInt("pid", i);
                    float px = particles.getFloat("px", i);
                    float py = particles.getFloat("py", i);
                    float pz = particles.getFloat("pz", i);
                    float vz = particles.getFloat("vz", i);
                    float momentum = (float) Math.sqrt(px * px + py * py + pz * pz);

                    if (pid == 11) {
                        electronCount++;
                        electronTotP += momentum;
                        double phi = Math.toDegrees(Math.atan2(py, px));
                        double theta = Math.toDegrees(Math.acos(pz / Math.sqrt(px * px + py * py + pz * pz)));
                        electronPhis.add(phi);
                        electronThetas.add(theta);
                        electronMomenta.add(new double[]{px, py, pz});
                        electronVzs.add((double) vz);
                        electron.setPxPyPzM(px, py, pz, 0.0005);
                    } else if (pid == 2212) {
                        protonCount++;
                        protonTotP += momentum;
                        double phi = Math.toDegrees(Math.atan2(py, px));
                        double theta = Math.toDegrees(Math.acos(pz / Math.sqrt(px * px + py * py + pz * pz)));
                        protonPhis.add(phi);
                        protonThetas.add(theta);
                        protonMomenta.add(new double[]{px, py, pz});
                        protonVzs.add((double) vz);
                        // Classify event by proton vz
                        if (vz > -8 && vz < -2) {
                            hasProtonInTarget = true;
                        } else {
                            hasProtonOutOfTarget = true;
                        }
                    }
                }

                // Fill per-event histograms for electrons and protons (as before)
                if (hasProtonInTarget || hasProtonOutOfTarget) {
                    if (electronCount > 0) {
                        hElectronNum.fill(electronCount);
                        hElectronTotP.fill(electronTotP);
                        for (double phi : electronPhis) hElectronPhi.fill(phi);
                        for (double theta : electronThetas) hElectronTheta.fill(theta);
                    }

                    hProtonNum.fill(protonCount);
                    hProtonTotP.fill(protonTotP);
                    for (double phi : protonPhis) hProtonPhi.fill(phi);
                    for (double theta : protonThetas) hProtonTheta.fill(theta);

                    // --- Bjorken z calculation and histogram filling ---
                    if (electronCount > 0) {
                        double nu = vBeam.e() - electron.e();
                        // Electron Bjorken z (use first electron)
                        if (!electronMomenta.isEmpty()) {
                            double[] eArr = electronMomenta.get(0);
                            double pe = Math.sqrt(eArr[0] * eArr[0] + eArr[1] * eArr[1] + eArr[2] * eArr[2]);
                            double Ee = Math.sqrt(pe * pe + 0.0005 * 0.0005);
                            double z_e = Ee / nu;
                            hElectronZ.fill(z_e);
                        }
                        // Proton Bjorken z for each proton
                        for (int ip = 0; ip < protonMomenta.size(); ip++) {
                            double[] pArr = protonMomenta.get(ip);
                            double px = pArr[0], py = pArr[1], pz = pArr[2];
                            double p = Math.sqrt(px * px + py * py + pz * pz);
                            double Ep = Math.sqrt(p * p + 0.938 * 0.938);
                            double z_p = Ep / nu;
                            hProtonZ.fill(z_p);

                            // Fill the correct 2D histogram for this proton
                            double vz = protonVzs.get(ip);
                            if (vz > -8 && vz < -2) {
                                hProtonNumVsZ_Deuterium.fill(z_p, protonCount);
                            } else {
                                hProtonNumVsZ_Carbon.fill(z_p, protonCount);
                            }
                        }
                    }

                    // --- Fill vz difference histogram for all e-p pairs ---
                    for (double evz : electronVzs) {
                        for (double pvz : protonVzs) {
                            hVzDiff.fill(evz - pvz);
                        }
                    }
                }
            }
        }

        // --- Visualization: draw all histograms on canvases and save as images ---

        TCanvas eCanvas = new TCanvas("Electron Variables", 1200, 800);
        eCanvas.divide(3, 2);
        eCanvas.cd(0).draw(hElectronNum);
        eCanvas.cd(1).draw(hElectronTotP);
        eCanvas.cd(2).draw(hElectronPhi);
        eCanvas.cd(3).draw(hElectronTheta);
        eCanvas.cd(4).draw(hElectronZ);
        eCanvas.save("/home/reh1/Pictures/LiquidD_DIS_Electron_Variables.png");

        TCanvas pCanvas = new TCanvas("Proton Variables", 1200, 800);
        pCanvas.divide(3, 2);
        pCanvas.cd(0).draw(hProtonNum);
        pCanvas.cd(1).draw(hProtonTotP);
        pCanvas.cd(2).draw(hProtonPhi);
        pCanvas.cd(3).draw(hProtonTheta);
        pCanvas.cd(4).draw(hProtonZ);
        pCanvas.save("/home/reh1/Pictures/LiquidD_DIS_Proton_Variables.png");

        // --- Plot and save number of protons vs z for each target ---
        TCanvas protonNumVsZDeutCanvas = new TCanvas("ProtonNumVsZ_Deuterium", 800, 600);
        protonNumVsZDeutCanvas.draw(hProtonNumVsZ_Deuterium);
        protonNumVsZDeutCanvas.save("/home/reh1/Pictures/LiquidD_DIS_ProtonNumVsZ_Deuterium.png");

        TCanvas protonNumVsZCarbonCanvas = new TCanvas("ProtonNumVsZ_Carbon", 800, 600);
        protonNumVsZCarbonCanvas.draw(hProtonNumVsZ_Carbon);
        protonNumVsZCarbonCanvas.save("/home/reh1/Pictures/LiquidD_DIS_ProtonNumVsZ_Carbon.png");

        // --- Project 2D histograms onto X (z) axis to get 1D histograms ---
        H1F hProtonZ_Deuterium = hProtonNumVsZ_Deuterium.projectionX();
        H1F hProtonZ_Carbon = hProtonNumVsZ_Carbon.projectionX();

// --- Divide Carbon by Deuterium to get the ratio histogram ---
        H1F hRatio = hProtonZ_Carbon.histClone("hRatio");
        hRatio.divide(hProtonZ_Deuterium);

        // --- Convert ratio histogram to GraphErrors and plot ---
        GraphErrors ratioGraph = hRatio.getGraph();
        ratioGraph.setTitle("Carbon/Deuterium Proton Count Ratio vs z");
        ratioGraph.setTitleX("Proton z");
        ratioGraph.setTitleY("C/D Ratio");
        ratioGraph.setMarkerStyle(2);
        ratioGraph.setMarkerSize(8);

        TCanvas ratioCanvas = new TCanvas("Ratio C/D", 800, 600);
        ratioCanvas.draw(ratioGraph, "AP"); // "AP" for data points only
        ratioCanvas.save("/home/reh1/Pictures/LiquidD_DIS_ProtonNumVsZ_Ratio.png");

        // --- Fit and plot the difference in vz between electron and proton ---
        F1D gaussVzDiff = new F1D("gaussVzDiff", "[amp]*exp(-0.5*((x-[mean])/[sigma])^2)", -6, 4);
        gaussVzDiff.setParameter(0, hVzDiff.getMax());
        gaussVzDiff.setParameter(1, hVzDiff.getMean());
        gaussVzDiff.setParameter(2, hVzDiff.getRMS());
        hVzDiff.fit(gaussVzDiff);

        TCanvas vzDiffCanvas = new TCanvas("Vz Difference", 600, 600);
        vzDiffCanvas.draw(hVzDiff);
        vzDiffCanvas.draw(gaussVzDiff, "same");
        vzDiffCanvas.save("/home/reh1/Pictures/LiquidD_DIS_VzDiff.png");

        System.out.println("Electron-Proton v_z Difference Gaussian Fit Mean: " + gaussVzDiff.getParameter(1));
        System.out.println("Electron-Proton v_z Difference Gaussian Fit Width (sigma): " + gaussVzDiff.getParameter(2));
        System.out.println("Done processing events.");
    }
}
