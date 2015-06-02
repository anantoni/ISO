/**
 * Created by anantoni on 1/5/2015.
 */

import factgen.FactGenerator;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBase;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.storage.IRelation;
import parser.ParseException;
import parser.SpigletParser;
import syntaxtree.Goal;
import transformer.Transformer;
import utilities.Triple;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Driver {

    public static void main(String[] args) throws EvaluationException {
        if (args.length != 5) {
            System.err.println("Please give input file");
            System.exit(-1);
        }
        else if (!args[0].endsWith(".spg")) {
            System.err.println("Please give a .spg file as input");
            System.exit(-1);
        }

        File spigletFile = new File(args[0]);
        String factsPath = args[1];
        String analysisLogicPath = args[2];
        String queriesPath = args[3];
        String optimizedSpigletPath = args[4];
        String projectFactsDir = factsPath + "/" + spigletFile.getName().replace(".spg", "/");

        String previousOptCode, currentOptCode = "";

        Transformer spigletTransformer = null;
        int counter = 0;
        do {
            previousOptCode = currentOptCode;
            Goal goal = null;
            try {
                SpigletParser spigletParser;
                if (counter == 0)
                   spigletParser  = new SpigletParser(new FileReader(args[0]));
                else
                    spigletParser = new SpigletParser(new ByteArrayInputStream(currentOptCode.getBytes(StandardCharsets.UTF_8)));
                goal = spigletParser.Goal();
                FactGenerator factGenerator = new FactGenerator(projectFactsDir);
                goal.accept(factGenerator, null);
                factGenerator.closeAllFiles();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (ParseException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            Parser parser = new Parser();
            Map<IPredicate, IRelation> factMap = new HashMap<>();

            final File factsDirectory = new File(projectFactsDir);
            factsDirectory.mkdir();

            if (factsDirectory.isDirectory())
                for (final File fileEntry : factsDirectory.listFiles()) {
                    if (fileEntry.isDirectory() || !fileEntry.getName().endsWith(".iris"))
                        System.out.println("Omitting file " + fileEntry.getPath());

                    else {
                        Reader factsReader;
                        try {
                            factsReader = new FileReader(fileEntry);
                            parser.parse(factsReader);
                        } catch (ParserException e) {
                            System.err.println("Parse exception in file: " + fileEntry.getName());
                            e.printStackTrace();
                            System.exit(-1);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        // Retrieve the facts and put all of them in factMap
                        factMap.putAll(parser.getFacts());
                }
            }
            else {
                System.err.println("Invalid facts directory path: " + projectFactsDir);
                System.exit(-1);
            }

            final File analysisLogicDir = new File(analysisLogicPath);
            List<IRule> rules = new ArrayList<>();
            if (analysisLogicDir.isDirectory())
                for (final File fileEntry : analysisLogicDir.listFiles()) {
                    if (fileEntry.isDirectory() || !fileEntry.getName().endsWith(".iris"))
                        System.out.println("Omitting file " + fileEntry.getPath());

                    else {
                        Reader rulesReader;
                        try {
                            rulesReader = new FileReader(fileEntry);
                            parser.parse(rulesReader);
                            rules.addAll(parser.getRules());
                        } catch (ParserException e) {
                            System.err.println("Parse exception in file: " + fileEntry.getName());
                            e.printStackTrace();
                            System.exit(-1);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        // Retrieve the facts and put all of them in factMap
                        factMap.putAll(parser.getFacts());
                    }
                }
            else {
                System.err.println("Invalid facts directory path: " + analysisLogicDir);
                System.exit(-1);
            }

            final File queriesDir = new File(queriesPath);
            List<IQuery> queries = new ArrayList<>();
            if (queriesDir.isDirectory())
                for (final File fileEntry : queriesDir.listFiles()) {
                    if (fileEntry.isDirectory() || !fileEntry.getName().endsWith(".iris"))
                        System.out.println("Omitting file " + fileEntry.getPath());

                    else {
                        Reader queriesReader;
                        try {
                            queriesReader = new FileReader(fileEntry);
                            parser.parse(queriesReader);
                            queries.addAll(parser.getQueries());
                        } catch (ParserException e) {
                            System.err.println("Parse exception in file: " + fileEntry.getName());
                            e.printStackTrace();
                            System.exit(-1);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        // Retrieve the facts and put all of them in factMap
                        factMap.putAll(parser.getFacts());
                    }
                }
            else {
                System.err.println("Invalid facts directory path: " + analysisLogicDir);
                System.exit(-1);
            }

            // Create a default configuration.
            Configuration configuration = new Configuration();

            // Enable Magic Sets together with rule filtering.
            configuration.programOptmimisers.add(new MagicSets());
            for (IRule rule : rules)
                configuration.ruleSafetyProcessor.process(rule);

            // Create the knowledge base.
            IKnowledgeBase knowledgeBase = new KnowledgeBase(factMap, rules, configuration);

            // Evaluate all queries over the knowledge base.
            Map<Triple<String, Integer>, Integer> constantMap = new HashMap<>();
            Map<Triple<String, Integer>, String> copyMap = new HashMap<>();
            Map<String, Set<Integer>> deadInstructionMap = new HashMap<>();

            for (IQuery query : queries) {
                List<IVariable> variableBindings = new ArrayList<>();
                IRelation relation = knowledgeBase.execute(query, variableBindings);

                // Output the variables.
                System.out.println("\n" + query.toString() + "\n" + variableBindings);

                // Output each tuple in the relation, where the term at position i
                // corresponds to the variable at position i in the variable
                // bindings list.
                for (int i = 0; i < relation.size(); i++) {
                    System.out.println(relation.get(i));

                    if (query.toString().contains("constantPropagation"))
                        constantMap.put(new Triple(relation.get(i).get(0).toString().replace("\'", ""), Integer.parseInt(relation.get(i).get(1).toString()), relation.get(i).get(2).toString().replace("\'", "")), Integer.parseInt(relation.get(i).get(3).toString()));

                    else if (query.toString().contains("copyPropagation"))
                        copyMap.put(new Triple(relation.get(i).get(0).toString().replace("\'", ""), Integer.parseInt(relation.get(i).get(1).toString()), relation.get(i).get(2).toString().replace("\'", "")), relation.get(i).get(3).toString().replace("\'", ""));

                    else if (query.toString().contains("deadVar")) {
                        String methodName = relation.get(i).get(0).toString().replace("\'", "");
                        if (deadInstructionMap.containsKey(methodName)) {
                            deadInstructionMap.get(methodName).add(Integer.parseInt(relation.get(i).get(1).toString()));

                        } else {
                            Set<Integer> methodDeadInstructionSet = new HashSet<>();
                            methodDeadInstructionSet.add(Integer.parseInt(relation.get(i).get(1).toString()));
                            deadInstructionMap.put(methodName, methodDeadInstructionSet);
                        }
                    }
                }
            }
            spigletTransformer = new Transformer(optimizedSpigletPath, spigletFile.getName().replace(".spg", "-opt.spg"), constantMap, copyMap, deadInstructionMap);
            goal.accept(spigletTransformer, null);
            currentOptCode = spigletTransformer.getOptCode();
            counter++;
            break;
        }
        while (!currentOptCode.equals(previousOptCode) || currentOptCode.equals(""));
        spigletTransformer.writeCode();
    }
}
