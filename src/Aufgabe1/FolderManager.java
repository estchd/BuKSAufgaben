package Aufgabe1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderManager {

    File folder;

    public FolderManager(String path, boolean relative) throws IOException
    {
        String folderPath;
        if(relative)
        {
            Path relativePath = Paths.get(path);
            Path absolutePath = relativePath.toAbsolutePath();
            folderPath = absolutePath.toString();
        }
        else{
            folderPath = path;
        }
        folder = new File(folderPath);
        if( !folder.exists() || !folder.canRead()){
            throw new IOException("Cannot Read Folder!");
        }
    }

    public List<String> listFilesInFolder() throws IOException
    {
        try
        {
            Stream<Path> walk = Files.walk(folder.toPath());
            return walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
        }
        catch(IOException ex)
        {
            throw new IOException("Could not List Files", ex);
        }
    }

    public List<String> getFileContent(String fileName) throws IOException
    {
        File file = new File(folder,fileName);
        if(!file.exists() || !file.canRead())
        {
            throw new IOException("Cannot Read File!");
        }
        return Files.readAllLines(file.toPath());
    }
}
