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

    private File file;

    private ArrayList<Integer> blanks;

    public FileConfiguration(LinkedHashMap<String, Object> contentMap) {
        super(contentMap);
    }
    
    public FileConfiguration() {
    	
    }

    /**
     * Creates a new {@link FileConfigurationNew} from a {@link File}
     */
    public FileConfiguration(File f) throws IOException {
    	this.file = f;
    	this.loadFile(f);
    }
    
    public void loadFile(File f) throws IOException {
        this.file = f;
        this.blanks = new ArrayList<>();
        this.contentMap = new LinkedHashMap<>();

        if ( !(f.isFile() && f.exists()) ) {
        	return;
        }
        
        List<String> list = Files.readAllLines(f.toPath());

        String section = "";
        int lastSpace = -1;
        
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

                int sp = (key.length() - key.trim().length());

                if (sp > lastSpace) {
                	lastSpace = sp;
                	section += "." + key.trim();
                }
                
                if (sp == lastSpace) {
                	if (section.indexOf('.') != -1) {
                		section = section.substring(0, section.lastIndexOf('.'));
                		section += "." + key.trim();
                	} else {
                		section = key.trim();
                	}
                }
                
                if (sp < lastSpace) {
                	lastSpace = sp;
                	section = section.substring(0, section.lastIndexOf('.'));
                	section = section.substring(0, section.lastIndexOf('.'));
                	section += "." + key.trim();
                }
                
                if (section.startsWith(".")) {
                	section = section.substring(1);
                }

                key = section;
                
                if (spl.length == 1 || spl[1].trim().length() == 0) {
                    if (i+1 < list.size() && list.get(i+1).trim().startsWith("-")) {
                        ArrayList<Object> o = new ArrayList<>();
                        int b = i;
                        
                        b++;
                        while (b < list.size() && list.get(b).trim().startsWith("-")) {
                            o.add(parseLine( list.get(b).trim().substring(1).trim() ));
                            b++;
                        }
                        contentMap.put(key, o);
                        continue;
                    }
                }
                
                if (spl.length == 1) {
                	continue;
                }
                
                String spl_1 = line.substring(line.indexOf(':') + 1);
                contentMap.put(key, parseLine( spl_1.trim() ));
            }
        }
    }

    @Override
    public void save() throws IOException {
        save(file);
    }
    
    public static String getTextAfterLastDot(String str) {
        int lastIndex = str.lastIndexOf(".");
        if (lastIndex != -1) {
            return str.substring(lastIndex + 1);
        } else {
            return "";
        }
    }

    @Override
    public void save(File to) throws IOException {
        ArrayList<String> s = new ArrayList<>();
        int a = 0;

        ArrayList<String> sectSet = new ArrayList<>();
        
        for (String key : contentMap.keySet()) {
            Object o = contentMap.get(key);
            if (key.contains(".")) {
                String[] k = key.split("[.]");
                int spaces = 0;
                String cc = "";
                
                for (String st : k) {
                	cc += "." + st;
                	if (cc.startsWith(".")) cc = cc.substring(1);
                	
                	if (sectSet.contains(cc)) {
                		spaces += 4;
                		continue;
                	}
                	sectSet.add(cc);

                	for (int i = 0; i < spaces; i ++) s.add(" ");

                    if (st.equals( key.substring(key.lastIndexOf('.')+1) )) {
                        if (o instanceof List) {
                            s.add( st + ": \n" );
                            String sp = repeat(" ", spaces + 2);
                            for (Object z : (List<?>)o) s.add(sp + "- " + z + "\n");
                        } else if (key.startsWith("#")) s.add( st.substring(0,st.lastIndexOf(':')) + "\n" ); else s.add( st + ": " + o + "\n" );
                    } else {
                    	s.add( st + ": " + "\n" );
                    }
                    spaces += 4;
                }
            } else {
            	sectSet.add(key);
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

    private Object parseLine(String val) {
        if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) return Boolean.valueOf(val);
        else {
            try { return Integer.valueOf(val);} catch (NumberFormatException ignore){}
            try { return Long.valueOf(val);   } catch (NumberFormatException ignore){}
            try { return Double.valueOf(val); } catch (NumberFormatException ignore){}
            return val;
        }
    }
    
    public String repeat(String str, int n) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < n; i++) {
    	    sb.append(str);
    	}
    	return sb.toString();
    }

}