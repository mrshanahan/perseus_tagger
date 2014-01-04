package edu.uchicago.perseus.tagger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import cc.mallet.pipe.*;
import cc.mallet.util.MalletLogger;

public class PerseusTagger {
    private static Logger logger = MalletLogger.getLogger(PerseusTagger.class.getName());
    private static final Pattern lexerPattern = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);
    
    Map<String, Object> parseArgs(String args) throws FileNotFoundException {
        Map<String, Object> arguments = new HashMap<String, Object>();
        for (String a : args) {
            // "Blessed" lemmas file, i.e. the file of the parses that should
            // take precedence over tagger output
            if ("-b".equals(a) || "--blessed".equals(a)) {
                File tmp_blessedParses = new File(a);
                if (!tmp_blessedParses.isReadable()) {
                    logger.fatal("blessed lemmata file \"%s\" not found or not readable", a);
                    throw new FileNotFoundException(a);
                }
                arguments.put("blessedFile", tmp_blessedParses);
            }
            // Default lemmas file, i.e. the file of parses that should
            // be assumed if parse could be ambiguous (obviously not for
            // all ambiguities; otherwise we wouldn't need a tagger!)
            else if ("-d".equals(a) || "--default".equals(a)) {
                File tmp_defaultParses = new File(a);
                if (!tmp_defaultParses.isReadable()) {
                    logger.fatal("default lemmata file \"%s\" not found or not readable", a);
                    throw new FileNotFoundException(a);
                }
                arguments.put("defaultParsesFile", tmp_defaultParses);
            }
            // Read data from STDIN
            else if ("--".equals(a)) {
                
            }
            // File containing training data
            else {
                File tmp_trainingInput = new File(a);
                if (!tmp_trainingInput.isReadable()) {
                    logger.fatal("training data file \"%s\" not found or not readable", a);
                    throw new FileNotFoundException(a);
                }
                if (arguments.get("trainingFiles") == null) {
                    ArrayList<String> trainingList = ArrayList<String>();
                    trainingList.append(tmp_trainingInput);
                    arguments.put("trainingFiles", tmp_trainingInput);
                } else {
                    arguments.get("trainingFiles").append(tmp_trainingInput);
                }
            }
        }
        return arguments;
    }

    public static void main(String[] args) {
        ArgumentParser argParser = ArgumentParser.getInstance();
        Map<String, Object> arguments = null;
        try {
            arguments = argParser.parseArgs(args);
        } catch (FileNotFoundException e) {
            System.err.printf("Error: could not read file %s\n", e.message);
            System.exit(-1);
        }

        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        pipeList.add(new Target2LabelSequence());
        pipeList.add(new CharSequence2TokenSequence(lexerPattern));
        pipeList.add(new TokenSequence2FeatureSequence());
        pipeList.add(new FeatureSequence2FeatureVector());
    }
}
