package com.severell.initializr.action.structure;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

public class StructureMapperResolver {

    private static final String RANDOM_ACCESS_MODE = "rw";
    private static  final FileChannel.MapMode FILE_CHANNEL_MODE = FileChannel.MapMode.READ_WRITE;
    private final short offset = 1;
    private final char separator = '\n';

    public List<StructureMapper> getFileMap(String filename, String searchKey){
        List<StructureMapper> mappers = null;
        try (RandomAccessFile fileIn = new RandomAccessFile(filename, "r")) {
            String lineBytes = null;int counter = 0;
            mappers = new ArrayList<>();
            while (lineBytes != null || counter++ == 0) {
                int startPos = (int) (fileIn.getFilePointer());
                lineBytes = fileIn.readLine();
                if(lineBytes != null) {
                    byte[] lineByteArray = lineBytes.getBytes();
                    String line = new String(lineByteArray, StandardCharsets.UTF_8);
                    if (line.contains(searchKey)) {
                        int endPos = lineByteArray.length + offset;
                        StructureMapper mapper = new StructureMapper(line, startPos, endPos);
                        mappers.add(mapper);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  mappers;
    }

    public int getOffset(){
        return offset;
    }

    public String getSeparator(){
        return String.valueOf(separator);
    }

    public boolean modifyOutFile(StructureMapper mapper){
        boolean modified = false;
        String compositePart = mapper.getContent().toString();
        try (RandomAccessFile file = new RandomAccessFile(mapper.getFilename(), RANDOM_ACCESS_MODE)) {
            MappedByteBuffer out = file.getChannel().map(FILE_CHANNEL_MODE, mapper.getStart(), compositePart.length());
            int counter = 0;
            while(counter < compositePart.length()){
                out.put((byte) compositePart.charAt(counter));
                counter++;
            }
            cleanBuffer(out);
            out.clear();
            modified = true;
        } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return modified;
    }

    public boolean modifyInFile(StructureMapper mapper)  {
        boolean modified = false;
        PrimitiveIterator.OfInt replacementIterator = mapper.getContent().chars().iterator();
        try (RandomAccessFile file = new RandomAccessFile(mapper.getFilename(), RANDOM_ACCESS_MODE)) {
            MappedByteBuffer out = file.getChannel().map(FILE_CHANNEL_MODE, mapper.getStart(), mapper.getLength());
            int counter = 0;
            while(counter < mapper.getLength() - offset){
                if(replacementIterator.hasNext()){
                    out.put((byte) replacementIterator.nextInt());
                }else{
                    out.put((byte)' ');
                }
                counter++;
            }
            out.put((byte)'\n');
            cleanBuffer(out);
            out.clear();
            modified = true;
        } catch (IOException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return modified;
    }

    public String getContent(String filename, long start) {
        byte[] readBytes = null;
        try (FileInputStream fileInput = new FileInputStream(filename)){
            long fileSize = fileInput.getChannel().size();
            ByteBuffer bytes = ByteBuffer.allocate((int) (fileSize - start));
            fileInput.getChannel().read(bytes, start);
            readBytes = bytes.array();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(readBytes, StandardCharsets.UTF_8);
    }

    String getReplacement(String line, String oldValue, String newValue){
        int start = line.indexOf(oldValue);
        int end = start + oldValue.length();
        return line.substring(0, start) + newValue + line.substring(end);
    }

    //https://stackoverflow.com/a/48821002/7958926 workaround for windows bug using reflection (does not close resource )
    //https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154
    private void cleanBuffer(ByteBuffer buffer) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
        invokeCleaner.invoke(unsafe, buffer);
    }
}
