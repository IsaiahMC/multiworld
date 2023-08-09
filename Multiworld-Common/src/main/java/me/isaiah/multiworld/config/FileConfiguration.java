/**
 * Isaiah's Configuration File Format
 * Tiny two file YAML-like configuration parser
 * 
 * Unlicense
 */
package me.isaiah.multiworld.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FileConfiguration extends Configuration {

    private String lastSection;
    private int space;
    private File file;

    private ArrayList<Integer> blanks;

    public FileConfiguration(LinkedHashMap<String, Object> contentMap) {
        super(contentMap);
    }

    /**
     * Creates a new {@link FileConfiguration} from a {@link File}
     */
    public FileConfiguration(File f) throws IOException {
        this.file = f;
        this.blanks = new ArrayList<>();
        this.contentMap = new LinkedHashMap<>();

        List<String> list = Files.readAllLines(f.toPath());
        this.lastSection = "";
        this.space = 0;

        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (line.startsWith("#")) {
                contentMap.put(line + ":" + i, i);
                continue;
            }
            if (line.isEmpty() || line.trim().isEmpty()) blanks.add(i);

            if (line.indexOf(':') != -1) {
                String[] spl = line.split("[:]");
                String key = spl[0];

                updateSection(key);

                if (spl.length == 1) {
                    if (i+1 < list.size() && list.get(i+1).trim().startsWith("-")) {
                        ArrayList<Object> o = new ArrayList<>();
                        i++;
                        while (i < list.size() && list.get(i).trim().startsWith("-")) {
                            o.add(parseLine( list.get(i).trim().substring(1).trim() ));
                            i++;
                        }
                        contentMap.put(key, o);
                    }
                    continue;
                }
                key = lastSection;
                contentMap.put(key, parseLine( spl[1].trim() ));
            }
        }
    }

    @Override
    public void save() throws IOException {
        save(file);
    }

    @Override
    public void save(File to) throws IOException {
        ArrayList<String> s = new ArrayList<>();
        int a = 0;
        String last3 = "", last2 = "", last1 = "";

        for (String key : contentMap.keySet()) {
            Object o = contentMap.get(key);
            if (key.contains(".")) {
                last3 = last2;
                last2 = key;
                String[] k = key.split("[.]");
                int spaces = 0;
                for (String st : k) {
                    for (int i = 0; i < spaces; i ++) s.add(" ");
                    if (st.equals( key.substring(key.lastIndexOf('.')+1) )) {
                        if (o instanceof List) {
                            s.add( st + ": \n" );
                            for (Object z : (List<?>)o) s.add("- " + z + "\n");
                        } else if (key.startsWith("#")) s.add( st.substring(0,st.lastIndexOf(':')) + "\n" ); else s.add( st + ": " + o + "\n" );
                    } else if (last2.startsWith(st) && last1.equals(st)) last1 = ""; else {
                        last1 = st;
                        if (!last3.startsWith(st)) s.add( st + ": " + "\n" );
                    }
                    spaces += 4;
                }
            } else {
                if (o instanceof List) {
                    s.add( key + ": \n" );
                    for (Object z : (List<?>)o) s.add("    - " + z + "\n");
                } else if (key.startsWith("#")) s.add( key.substring(0,key.lastIndexOf(':')) + "\n" ); else s.add( key + ": " + o + "\n" );
            }
            a++;
        }
        String raw = "";
        for (String r : s) raw += r;
        String zz = "";
        a = 0;
        for (String z : raw.split("\n")) {
            if (blanks.contains(a)) {
                zz += "\n";
                a++;
            }
            zz += z + "\n";
            a++;
        }
        Files.write(to.toPath(), zz.getBytes());
    }

    private void updateSection(String key) {
        int sp = (key.length() - key.trim().length());
        if (sp == 0) {
            lastSection = key.trim();
        } else if (lastSection.contains(".")) lastSection = sp > space ? lastSection + "." + key.trim() :
            lastSection.substring(0, lastSection.lastIndexOf('.')) + "." + key.trim(); else lastSection += "." + key.trim();
        space = sp;
    }

    private Object parseLine(String val) {
        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) return Boolean.valueOf(val);
        else {
            try { return Integer.valueOf(val);} catch (NumberFormatException ignore){}
            try { return Long.valueOf(val);   } catch (NumberFormatException ignore){}
            try { return Double.valueOf(val); } catch (NumberFormatException ignore){}
            return val;
        }
    }

}