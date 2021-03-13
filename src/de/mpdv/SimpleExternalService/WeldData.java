package de.mpdv.SimpleExternalService;

import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesException;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.data.SesResultBuilder;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
import de.mpdv.sdi.systemutility.IDataTableBuilder;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


public class WeldData implements ISimpleExternalService {
    
    

    PrintWriter writer = null;
    
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {

    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");

        try 
        {
            writer = new PrintWriter("C:\\WeldDataProject.Log", "UTF-8");
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (UnsupportedEncodingException ex) 
        {
            Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
	builder.addCol("inspectionpoint.result.id", DataType.STRING);
	builder.addCol("machine.designation", DataType.STRING);
	builder.addCol("operation.latest_end_ts", DataType.STRING);
	builder.addCol("order.id", DataType.STRING);
	builder.addCol("person.infotext20", DataType.STRING);
	builder.addCol("person.name", DataType.STRING  );
        builder.addCol("order.article", DataType.STRING  );
        
        String strOrder = "";
        String strArtkl = "";
        
        SpecialParam u_order = request.getSpecialParam("order.id");
        SpecialParam u_artkl = request.getSpecialParam("order.article");
        SpecialParam u_date = request.getSpecialParam("operation.latest_end_ts");
        
        writer.println("u_order="+u_order);
        writer.println("u_artkl="+u_artkl);
        
        if (u_order != null) strOrder = (String) u_order.getValue();
	if (u_artkl != null) strArtkl = (String) u_artkl.getValue();
        
        List<SmbFile> FileList = new ArrayList<SmbFile>();
        if (!"".equals(strOrder) && !"".equals(strArtkl))
        {
            try 
            {
                FileList = findFileForOrder(strOrder, strArtkl, logger);
            }
            catch (SmbException ex) 
            {
                Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                writer.println("FileList err="+ex.getMessage());
            }
            finally
            {
                if (!FileList.isEmpty())
                {
                    try
                    {   
                        for (SmbFile dFile : FileList)
                        {
                            long s = System.currentTimeMillis();
                            ReadS33File(dFile);
                            long e = System.currentTimeMillis();
                            
                            writer.println((e - s) / 1000);
                            
                            builder.addRow();
                            builder.value(strOrder);
                            builder.value(strArtkl);
                            builder.value("");
                            builder.value("");
                            builder.value("");
                            builder.value("");
                            builder.value("");
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Exception while accessing database", e);
                        throw new SesException("lkDbError",	"Exception while accessing database", e);
                    } 
                }
            } 
        }

        if (writer != null) writer.close();
        
        return new SesResultBuilder().addDataTable(builder.build()).build();
    }
    
    private List<SmbFile> findFileForOrder(final String mesOrder, String OrderArticle, final ISdiLogger logger) throws SmbException
    {
        List<SmbFile> FolderList = new ArrayList<SmbFile>();
        
        String tType = OrderArticle.substring(0, OrderArticle.indexOf(" "));
        String[] ItemData = OrderArticle.substring(OrderArticle.indexOf(" ") + 1, OrderArticle.length() - OrderArticle.indexOf(" ") - 1).split("[.]");
            
        writer.println("mesOrder="+mesOrder);
        writer.println("OrderArticle="+OrderArticle);
        writer.println("tType="+tType);
        writer.println(OrderArticle.substring(OrderArticle.indexOf(" ") + 1, OrderArticle.length() - OrderArticle.indexOf(" ") - 1));
        writer.println(String.format("ItemData.length=%1$d", ItemData.length));
            
        String oType = "";
        String D = "";
        String d = "";
        String S = "";
            
        if ("О ПФ90".contains(tType))
        {
            oType = "Отвод";
            D = ItemData[0];
            S = ItemData[1];
        }

        if (!"".equals(D) && !"".equals(S) && !"".equals(oType))
        {
            String url = "smb://10.58.16.100/Archive/";
            String username = "operator";
            String password = "operator";

            NtlmPasswordAuthentication auth = null;
            try
            {
                auth = new NtlmPasswordAuthentication("10.58.16.100" , username, password);
                writer.println("auth=" + auth.toString());
            }
            catch (Exception e)
            {
                auth = null;
                logger.error("Exception while run NtlmPasswordAuthentication", e);
                writer.println("err=" + e.getMessage());
            }
            finally
            {
                if (auth != null)
                {
                    SmbFile ArchDir = null;
                    try 
                    {
                        ArchDir = new SmbFile(url, auth);
                        writer.println("dir=" + ArchDir.toString());
                    } 
                    catch (MalformedURLException e) 
                    {
                        ArchDir = null;
                        logger.error("Exception while create ArchDir", e);
                        writer.println("ArchDir err=" + e.getMessage());
                    }
                    finally
                    {
                        if (ArchDir != null)
                        {
                            SmbFile[] ProductDir = null;
                            try 
                            {
                                ProductDir = ArchDir.listFiles();
                                writer.println(String.format("smbDir=%1$d", ProductDir.length));
                            } 
                            catch (SmbException ex) 
                            {
                                ProductDir = null;
                                Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                                writer.println(String.format("ProductDir err=%1$s", ex.getMessage()));
                            }
                            finally
                            {
                                if (ProductDir != null)
                                {
                                    if (ProductDir.length != 0)
                                    {
                                        for(SmbFile pDir : ProductDir)
                                        {
                                            if (pDir.isDirectory() && pDir.getName().contains(D) && pDir.getName().contains(S) && pDir.getName().toLowerCase().contains(oType.toLowerCase()))
                                            {
                                                writer.println(String.format("f.name=%1$s", pDir.getName()));
                                                SmbFile[] DateDir = null;
                                                try
                                                {
                                                    DateDir = pDir.listFiles();
                                                }
                                                catch (SmbException e) 
                                                {
                                                    DateDir = null;
                                                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, e);
                                                    writer.println(String.format("f.listFiles err=%1$s", e.getMessage()));
                                                }
                                                finally
                                                {
                                                    if (DateDir != null) 
                                                    {
                                                        if (DateDir.length != 0) 
                                                        {
                                                            for (SmbFile sf: DateDir) 
                                                            {
                                                                if (sf.isDirectory())
                                                                {
                                                                    SmbFile[] DataFile = sf.listFiles();
                                                                    
                                                                    if (DataFile != null)
                                                                    {
                                                                        if (DataFile.length != 0)
                                                                        {
                                                                            for (SmbFile f: DataFile)
                                                                            {
                                                                                if (f.getName().endsWith("s33") && f.getName().contains(mesOrder.substring(0, mesOrder.indexOf(".")).replaceAll("\\D+", "")))
                                                                                {
                                                                                    FolderList.add(f);
                                                                                    writer.println(f.getPath());
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }    
        }
        return FolderList;
    }
    
    private String ReadS33File(SmbFile S33File) 
    {
        String res = "";
        java.io.InputStream stream = null;
        try 
        {
            stream = S33File.getInputStream(); 
        } 
        catch (Exception e) 
        {
            Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, e);
            writer.println("S33File.getInputStream err="+e.getMessage());
        }
        finally
        {
            
            if (stream != null)
            {
                
                byte[] dataArry = new byte[512];
                writer.println("stream.toString()="+stream.toString());
                
                try {
                    int l = stream.read(dataArry, 0, 512);
                    
                    if (l != 0)
                    {
                        writer.println(String.format("dataArry=%1$b",dataArry));
                        long ld = 0xd0cf11ea1b11ae1L;
                        writer.println("0xd0cf11ea1b11ae1="+ld);
                        //if ())
                    }
                    
                    
                    /*POIFSFileSystem fs = null;
                    try
                    {
                    fs = new POIFSFileSystem(stream);
                    }
                    catch (Exception e)
                    {
                    writer.println("POIFSFileSystem(stream) err="+e.getMessage());
                    // an I/O error occurred, or the InputStream did not provide a compatible
                    // POIFS data structure
                    }
                    finally
                    {
                    if (fs != null)
                    {
                    writer.println("fs.toString="+fs.toString());
                    
                    DirectoryEntry root = null;
                    try
                    {
                    root = fs.getRoot();
                    }
                    catch (Exception e)
                    {
                    writer.println("root.getName err="+e.getMessage());
                    }
                    finally
                    {
                    if (root != null)
                    {
                    writer.println("root.getName()="+root.getName());
                    Entry sMeta = null;
                    try {
                    sMeta = root.getEntry("TESTRON_S33_META");
                    } catch (Exception ex) {
                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                    writer.println("root.getEntry(TESTRON_S33_META) err="+ex.getMessage());
                    }
                    finally
                    {
                    if (sMeta != null)
                    {
                    DocumentInputStream dStream;
                    try
                    {
                    dStream = new DocumentInputStream((DocumentEntry) sMeta);
                    }
                    catch (IOException ex)
                    {
                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    long sSize = 0;
                    try
                    {
                    sSize = stream.available();
                    }
                    catch (IOException ex)
                    {
                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    // process data from stream
                    byte[] content = new byte[]{};
                    try {
                    stream.read(content);
                    } catch (IOException ex) {
                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                    stream.close();
                    } catch (IOException ex) {
                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    for (int i = 0; i < content.length; i++)
                    {
                    int c = content[i];
                    if (c < 0)
                    {
                    c = 0x100 + c;
                    }
                    }
                    writer.println(content);
                    }
                    }
                    }
                    }
                    }
                    }*/
                } catch (IOException ex) {
                    Logger.getLogger(WeldData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return res;
    }
}



