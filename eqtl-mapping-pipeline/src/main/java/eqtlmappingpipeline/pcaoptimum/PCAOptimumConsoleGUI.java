/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eqtlmappingpipeline.pcaoptimum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import umcg.genetica.console.ConsoleGUIElems;
import umcg.genetica.io.Gpio;

/**
 *
 * @author harmjan
 */
public class PCAOptimumConsoleGUI {

    public PCAOptimumConsoleGUI(String[] args) {

        String settingsfile = null;
        String settingstexttoreplace = null;
        String settingstexttoreplacewith = null;
        String in = null;
        String out = null;
        boolean cis = true;
        boolean trans = false;
        int perm = 10;
        String outtype = "text";
        String inexp = null;
        String inexpplatform = null;
        String inexpannot = null;
        String gte = null;
        String snpfile = null;
        Integer threads = null;

        String transsnps = null;
        String cissnps = null;

        boolean performEigenvectorQTLMapping = false;
        boolean inventorize = false;
        boolean inventorizepcqtl = false;
        boolean covariatesremoved = false;
        boolean runonlypcqtlnormalization = false;
        Integer runOnlyNumPCsRemoved = null;

        Integer nrEQTLsToOutput = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String val = null;

            if (i + 1 < args.length) {
                val = args[i + 1];
            }

            if (arg.equals("--settings")) {
                settingsfile = val;
            } else if (arg.equals("--replacetext")) {
                settingstexttoreplace = val;
            } else if (arg.equals("--replacetextwith")) {
                settingstexttoreplacewith = val;
            } else if (arg.equals("--in")) {
                in = val;
            } else if (arg.equals("--out")) {
                out = val;
            } else if (arg.equals("--inexp")) {
                inexp = val;
            } else if (arg.equals("--inexpplatform")) {
                inexpplatform = val;
            } else if (arg.equals("--inexpannot")) {
                inexpannot = val;
            } else if (arg.equals("--gte")) {
                gte = val;
            } else if (arg.equals("--pcqtl")) {
                performEigenvectorQTLMapping = true;
            } else if (arg.equals("--inventorize")) {
                inventorize = true;
            } else if (arg.equals("--inventorize-pcqtl")) {
                inventorize = true;
                inventorizepcqtl = true;
            } else if (arg.equals("--covariatesremoved")) {
                covariatesremoved = true;
            } else if (arg.equals("--cis")) {
                cis = true;
            } else if (arg.equals("--cistrans")) {
                cis = true;
                trans = true;
            } else if (arg.equals("--trans")) {
                trans = true;
            } else if (arg.equals("--transsnps")) {
                transsnps = val;
            } else if (arg.equals("--cissnps")) {
                cissnps = val;
            } else if (arg.equals("--onlynormalize")) {
                runonlypcqtlnormalization = true;
            } else if (arg.equals("--maponpc")) {
                try {
                    runOnlyNumPCsRemoved = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    System.err.println("Error --onlymapqtlsonpcsremoved should be an integer");
                    System.exit(-1);
                }

            } else if (arg.equals("--perm")) {
                try {
                    perm = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    System.err.println("Error --perm should be an integer");
                    System.exit(-1);
                }
            } else if (arg.equals("--threads")) {
                try {
                    threads = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    System.err.println("Error --threads should be an integer");
                    System.exit(-1);
                }
            } else if (arg.equals("--maxresults")) {
                try {
                    nrEQTLsToOutput = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    System.err.println("Error --maxresults should be an integer");
                }

            }
        }

        try {
            if (inventorize || inventorizepcqtl) {
                if (in == null) {
                    System.out.println("If summarizing directory output, please also supply location of directory using --in");
                } else {

                    PCAOptimumInventorize p = new PCAOptimumInventorize();

                    //MJ
                    String[] fileList = Gpio.getListOfFiles(in);
                    ArrayList<Integer> pcs = new ArrayList<Integer>();
                    for (String f : fileList) {

                        if (f.endsWith("PCAsRemoved")) {
                            File t = new File(f);
                            String tmp = t.getName();
                            tmp = tmp.replace("Cis-", "");
                            tmp = tmp.replace("Trans-", "");
                            tmp = tmp.replace("PCAsRemoved", "");

                            pcs.add(Integer.parseInt(tmp));
                        }
                        //System.out.println(f);
                    }
                    Collections.sort(pcs);

                    int max = 0;
                    int stepSize = 0;
                    for (int i = 0; i < (pcs.size() - 1); i++) {

                        if (i == 0) {
                            if (pcs.get(pcs.size() - 1) > max) {
                                max = pcs.get(pcs.size() - 1);
                            }
                        }
                        if (pcs.get(i) > max) {
                            max = pcs.get(i);
                        }
                        stepSize += pcs.get(i + 1) - pcs.get(i);
                    }

                    if(pcs.isEmpty()){
                        System.out.println("No PCA corrected files."
                                + "\n Please first run the normalization procedure.");
                        System.exit(0);
                    }

                    if ((((double) stepSize / (pcs.size() - 1)) % 1) != 0) {
                        System.out.println("Step size is invalid."
                                + "\n Please look in to the input directory for missing files");
                        System.out.println((((double) stepSize / (pcs.size() - 1)) % 1));
                        System.out.println("Determined max: "+max);
                        System.out.println("Determined stepsize: "+stepSize);
                        System.exit(0);
                    }
                    stepSize = (int) ((double) stepSize / (pcs.size() - 1));

                    //MJ

                    if (!inventorizepcqtl) {
                        p.inventory(in, cis, trans, max, stepSize);
                    } else {
                        p.inventorypcqtl(in, cis, trans, max, stepSize);
                    }
                }
            } else {
                if (settingsfile == null && (in == null || inexp == null || out == null)) {
                    System.out.println("ERROR: Please supply settings file (--settings settings.xml) or --in, --out and --inexp");
                    printUsage();
                } else if (cissnps == null || transsnps == null || !Gpio.exists(cissnps) || !Gpio.exists(transsnps)) {
                    if (cissnps == null || transsnps == null) {
                        System.out.println("ERROR: you must supply your set of SNPs to test for the optimal number of PCs to remove. ");
                        System.out.println("Defined values: ");
                        System.out.println("--cissnps: " + cissnps);
                        System.out.println("--transsnps: " + transsnps);
                    } else {
                        if (!Gpio.exists(cissnps)) {
                            System.out.println("Could not find file: " + cissnps);
                        }

                        if (!Gpio.exists(transsnps)) {
                            System.out.println("Could not find file: " + transsnps);
                        }
                    }
                } else {
                    if (performEigenvectorQTLMapping) {
                        if (runonlypcqtlnormalization) {
                            runOnlyNumPCsRemoved = null;
                        }

                        PCAOptimumGeneticVectors p = new PCAOptimumGeneticVectors();

                        p.setSNPSets(cissnps, transsnps);
                        p.setCovariatesRemoved(covariatesremoved);
                        p.run(settingsfile, settingstexttoreplace, settingstexttoreplacewith,
                                in, inexp, inexpplatform, inexpannot, gte, out, cis, trans, perm, true, false, snpfile, threads, runonlypcqtlnormalization, runOnlyNumPCsRemoved);
                    } else {
                        PCAOptimum p = new PCAOptimum();
                        p.setSNPSets(cissnps, transsnps);
                        p.setCovariatesRemoved(covariatesremoved);
                        p.initialize(settingsfile, settingstexttoreplace, settingstexttoreplacewith, in, inexp, inexpplatform, inexpannot, gte, out, cis, trans, perm, true, false, snpfile, threads, nrEQTLsToOutput, null, null);
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void printUsage() {
        System.out.println("");
        System.out.print("Command line options:\n" + ConsoleGUIElems.LINE);
        System.out.println("--in\t\t\tdir\t\tLocation of the genotype data\n"
                + "--out\t\t\tdir\t\tLocation where the output should be stored\n"
                + "--inexp\t\t\tstring\t\tLocation of expression data\n"
                + "--inexpplatform\t\tstring\t\tGene expression platform\n"
                + "--inexpannot\t\tstring\t\tLocation of annotation file for gene expression data\n"
                + "--gte\t\t\tstring\t\tLocation of genotype to expression coupling file\n"
                + "--cis\t\t\tstring\t\tDo a cis analysis (default)\n"
                + "--trans\t\t\tstring\t\tDo a trans analysis\n"
                + "--cistrans\t\tstring\t\tDo both a cis and a trans analysis\n"
                + "--threads\t\tinteger\t\tNumber of threads to calculate with. Default is number of processors.\n"
                + "--pcqtl\t\t\t\t\tPerform QTL mapping on eigenvectors, repeat PCA removal, and don't remove eigenvectors under genetic control\n"
                + "--inventorize\t\tdir\t\tSummarize the PC optimum results for a certain outputdirectory\n"
                + "--inventorize-pcqtl\tdir\t\tSummarize the PC optimum results for a certain outputdirectory\n"
                + "--cissnps\t\tstring\t\tList of SNPs to test in cis\n"
                + "--transsnps\t\tstring\t\tList of SNPs to test in trans\n"
                
                +"\nSpecific options for --pcqtl:\n"
                + "--covariatesremoved\t\t\tIndicate whether covariates were removed\n"
                + "--onlynormalize\t\t\t\tOnly perform the pcqtl mapping and subsequent normalization\n"
                + "--maponpc\t\tInteger\t\tOnly perform the eQTL mapping on the nth PC removed\n"
                );
        System.out.println("");
    }
    
    
}