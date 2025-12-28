package com.exlibris.dps.repository.plugin;

import com.exlibris.core.infra.common.exceptions.logging.ExLogger;
import com.exlibris.core.sdk.utils.FSUtil;
import com.exlibris.dps.sdk.techmd.FormatValidationPlugin;
import com.exlibris.dps.sdk.techmd.MDExtractorPlugin;
import edu.harvard.hul.ois.jhove.*;

import java.io.File;
import java.util.*;

public class JPEGHULMDExtractorAndValidator implements FormatValidationPlugin, MDExtractorPlugin {
    private static ExLogger log = ExLogger
            .getExLogger(JPEGHULMDExtractorAndValidator.class);

    protected static String jhoveConfigFileName = FSUtil.getSystemDir()
            + "conf/jhove.conf";
    public static App jhoveApplication = new App(
            "Jhove",
            "1.1 (pre-release g)",
            new int[] { 2007, 8, 30 },
            ("java Jhove [-c config] [-m module] [-h handler] [-e encoding] [-H handler] [-o output] [-x saxclass] [-t tempdir] [-b bufsize] [-l loglevel] [[-krs] dir-file-or-uri [...]]"),
            ("Copyright 2004-2007 by the President and Fellows of Harvard College. Released under the GNU Lesser General Public License."));

    public RepInfo repinfo = null;
    protected List<String> errorMessages = null;
    protected List<String> errorIds = null;
    private String jhoveModule = null;
    private String pluginVersion;

    private static final String PLUGIN_VERSION_INIT_PARAM = "PLUGIN_VERSION_INIT_PARAM";
    private static final String JHOVE_MODULE = "jhoveModule";


    private static String JPEGjhoveModule =  "JPEG-hul";
    private static String multiPagePropertyToCheck = "Images";
    private static String multiPagePropertyToCount = "Image";

    private static final String REG_SA_JHOVE = "REG_SA_JHOVE";

    private Integer multiPageSize = 0;

    private static List<String> attList = new ArrayList<String>();
    static {
        attList.add("JPEGMetadata.ApplicationSegments");
        attList.add("JPEGMetadata.Comments");
        attList.add("JPEGMetadata.CompressionType");
        attList.add("JPEGMetadata.Images.Image.Exif.ApertureValue");
        attList.add("JPEGMetadata.Images.Image.Exif.ColorSpace");
        attList.add("JPEGMetadata.Images.Image.Exif.ComponentsConfiguration");
        attList.add("JPEGMetadata.Images.Image.Exif.CompressedBitsPerPixel");
        attList.add("JPEGMetadata.Images.Image.Exif.CustomRendered");
        attList.add("JPEGMetadata.Images.Image.Exif.DateTimeOriginal");
        attList.add("JPEGMetadata.Images.Image.Exif.ExifVersion");
        attList.add("JPEGMetadata.Images.Image.Exif.ExposureBiasValue");
        attList.add("JPEGMetadata.Images.Image.Exif.ExposureProgram");
        attList.add("JPEGMetadata.Images.Image.Exif.ExposureTime");
        attList.add("JPEGMetadata.Images.Image.Exif.FileSource");
        attList.add("JPEGMetadata.Images.Image.Exif.Flash");
        attList.add("JPEGMetadata.Images.Image.Exif.FlashpixVersion");
        attList.add("JPEGMetadata.Images.Image.Exif.FNumber");
        attList.add("JPEGMetadata.Images.Image.Exif.FocalLength");
        attList.add("JPEGMetadata.Images.Image.Exif.FocalLengthIn35mmFilm");
        attList.add("JPEGMetadata.Images.Image.Exif.FocalPlaneResolutionUnit");
        attList.add("JPEGMetadata.Images.Image.Exif.LightSource");
        attList.add("JPEGMetadata.Images.Image.Exif.MakerNote");
        attList.add("JPEGMetadata.Images.Image.Exif.MaxApertureValue");
        attList.add("JPEGMetadata.Images.Image.Exif.MeteringMode");
        attList.add("JPEGMetadata.Images.Image.Exif.PixelXDimension");
        attList.add("JPEGMetadata.Images.Image.Exif.PixelYDimension");
        attList.add("JPEGMetadata.Images.Image.Exif.Saturation");
        attList.add("JPEGMetadata.Images.Image.Exif.SceneCaptureType");
        attList.add("JPEGMetadata.Images.Image.Exif.SceneType");
        attList.add("JPEGMetadata.Images.Image.Exif.Sharpness");
        attList.add("JPEGMetadata.Images.Image.Exif.ShutterSpeedValue");
        attList.add("JPEGMetadata.Images.Image.Exif.SubjectDistance");
        attList.add("JPEGMetadata.Images.Image.Exif.SubjectDistanceRange");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.AutoFocus");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.BackLight");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.BitsPerSample");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Brightness");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ByteOrder");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ChecksumMethod");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ChecksumValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Class");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColormapBitCodeValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColormapBlueValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColormapGreenValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColormapRedValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColormapReference");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColorSpace");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ColorTemp");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.CompressionLevel");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.CompressionScheme");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.DateTimeCreated");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.DateTimeProcessed");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.DeviceSource");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.DigitalCameraManufacturer");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.DigitalCameraModel");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.DisplayOrientation");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ExposureBias");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ExposureIndex");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ExposureTime");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ExtraSamples");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.FileSize");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Flash");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.FlashEnergy");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.FlashReturn");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.FNumber");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.FocalLength");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.GrayResponseCurve");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.GrayResponseUnit");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.HostComputer");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ImageData");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ImageIdentifier");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ImageIdentifierLocation");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ImageLength");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ImageProducer");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ImageWidth");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.MeteringMode");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Methodology");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.MimeType");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Orientation");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.OS");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.OSVersion");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PerformanceData");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PixelSize");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PlanarConfiguration");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PreferredPresentation");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PrimaryChromaticitiesBlueX");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PrimaryChromaticitiesBlueY");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PrimaryChromaticitiesGreenX");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PrimaryChromaticitiesGreenY");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PrimaryChromaticitiesRedX");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.PrimaryChromaticitiesRedY");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ProcessingActions");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ProcessingAgency");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ProcessingSoftwareName");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ProcessingSoftwareVersion");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ProfileName");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Profiles");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ProfileURL");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ReferenceBlackWhite");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.RowsPerStrip");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SamplesPerPixel");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SamplingFrequencyPlane");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SamplingFrequencyUnit");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ScannerManufacturer");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ScannerModelName");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ScannerModelNumber");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ScannerModelSerialNo");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ScanningSoftware");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ScanningSoftwareVersionNo");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SceneIlluminant");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SegmentType");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.Sensor");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceData");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceID");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceType");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceXDimension");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceXDimensionUnit");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceYDimension");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SourceYDimensionUnit");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.StripByteCounts");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.StripOffsets");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.SubjectDistance");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TargetIDManufacturer");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TargetIDMedia");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TargetIDName");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TargetIDNo");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TargetType");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TileByteCounts");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TileLength");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TileOffsets");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.TileWidth");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.ViewerData");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.WhitePointXValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.WhitePointYValue");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.XPhysScanResolution");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.XPrintAspectRatio");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.XSamplingFrequency");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.XTargetedDisplayAR");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YCbCrCoefficients");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YCbCrPositioning");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YCbCrSubSampling");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YPhysScanResolution");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YPrintAspectRatio");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YSamplingFrequency");
        attList.add("JPEGMetadata.Images.Image.NisoImageMetadata.YTargetedDisplayAR");
        attList.add("JPEGMetadata.Images.Image.PixelAspectRatio.PixelAspectRatioX");
        attList.add("JPEGMetadata.Images.Image.PixelAspectRatio.PixelAspectRatioY");
        attList.add("JPEGMetadata.Images.Image.QuantizationTables.QuantizationTable.DestinationIdentifier");
        attList.add("JPEGMetadata.Images.Image.QuantizationTables.QuantizationTable.Precision");
        attList.add("JPEGMetadata.Images.Image.RestartInterval");
        attList.add("JPEGMetadata.Images.Image.Scans");
        attList.add("JPEGMetadata.Images.Number");
        attList.add("JPEGMetadata.Images.Image.XMP");

    }

    public void initParams(Map<String, String> params) {
        jhoveModule = params.get(JHOVE_MODULE);
        pluginVersion = params.get(PLUGIN_VERSION_INIT_PARAM);
    }

    public boolean validateFormat(String filepath) {
        JhoveBase je;
        try {
            je = new JhoveBase();
            je.setLogLevel("SEVERE");
            je.init(jhoveConfigFileName, null);
            File file = new File(filepath);
            String tempDir = je.getTempDirectory();
            createTempDir(tempDir);
            // use appropriate module (specified in the mapping)
            Module module = je.getModule(jhoveModule);
            // actually process
            JPEGHULMDExtractorAndValidator.DpsHandler dpsHandler = new JPEGHULMDExtractorAndValidator.DpsHandler();
            je.process(jhoveApplication, module, dpsHandler, file.getAbsolutePath());
            repinfo = dpsHandler.getRepinfo();
            logRepinfoMessages();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return isValid();
    }

    @Override
    public String getAgent() {
        return getAgentName() + " Format Validation and Extraction, Plugin Version " + pluginVersion;
    }

    public String getProfile(){
        return null;
    }


    @Override
    public boolean isValid() {
        if (repinfo == null)
            return false;

        if (repinfo.getValid() == 0)
            return false;

        return true;
    }

    @Override
    public boolean isWellFormed() {
        if (repinfo == null)
            return false;

        if (repinfo.getWellFormed() == 0)
            return false;

        return true;
    }

    @Override
    public List<String> getErrors() {
        return errorMessages;
    }

    @Override
    public List<String> getErrorIds() {
        return errorIds;
    }

    private void logRepinfoMessages() {
        if (repinfo == null)
            return;

        List<Message> list = repinfo.getMessage();
        if (list == null)
            return;

        for (Message message : list) {
            log.warn("jhove message: " + message.getMessage());
            if (message instanceof ErrorMessage) {
                addError(message.getId(), message.getMessage());
            }
        }
    }

    public void createTempDir(String tempDir) {

        File jhoveTempDir = new File(tempDir);
        if (!jhoveTempDir.exists()) {
            jhoveTempDir.mkdirs();
        }
    }

    protected void addError(String id, String message) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<String>();
        }
        if (errorIds == null) {
            errorIds = new ArrayList<String>();
        }
        errorMessages.add(message);
        if (id != null)
            errorIds.add(id);
    }

    public String getAgentName()
    {
        return "Extractor/Validator JHOVE , " + jhoveModule;
    }

    public class DpsHandler extends HandlerBase {

        private RepInfo repinfo;

        public DpsHandler() {
            super("DPSHandler", "v1", new int[3], "note", "right");
        }

        public RepInfo getRepinfo() {
            return this.repinfo;
        }

        public void show() {}

        public void show(Module module) {}

        public void show(RepInfo repinfo) {
            this.repinfo = repinfo;
        }

        public void show(OutputHandler outputhandler) {}

        public void show(App app) {}

        public void showFooter() {}

        public void showHeader() {}
    }

    @Override
    public void extract(String fileName) throws Exception {
        extract(fileName,JPEGjhoveModule, multiPagePropertyToCheck, multiPagePropertyToCount);
    }

    @Override
    public List<String> getSupportedAttributeNames() {
        return attList;
    }

    public void extract(String fileName, String jhoveModule) throws Exception
    {
        this.extract(fileName, jhoveModule, null, null);
    }

    public void extract(String fileName, String jhoveModule, String multiplePropertiesToCheck, String muliplePropertiesToCount) throws Exception {

        JhoveBase je = new JhoveBase();
        je.setLogLevel("SEVERE");
        je.init(jhoveConfigFileName, null);
        File file = new File(fileName);
        String tempDir = je.getTempDirectory();
        createTempDir(tempDir);
        // use appropriate module (specified in the mapping)
        Module module = je.getModule(jhoveModule);
        // actually process
        JPEGHULMDExtractorAndValidator.DpsHandler dpsHandler = new JPEGHULMDExtractorAndValidator.DpsHandler();
        je.process(jhoveApplication, module, dpsHandler , file.getAbsolutePath());
        repinfo = dpsHandler.getRepinfo();
        logRepinfoMessages();

        //if no property is returned - throw exception
        if ((repinfo.getProperty() == null) || (repinfo.getProperty().isEmpty())) {
            addError(null, "Failed to retrieve extractor properties");
        }

        //throw new DigitoolException(DescriptorConstants.GN_UnexpectedError, "JHOVE Tech Md Extractor failed to retrieve extractor properties");

    }

    public void setImageCount(int count){
        this.multiPageSize = count;
    }

    public RepInfo getRepinfo() {
        return repinfo;
    }

    public Integer getImageCount(){
        return this.multiPageSize;
    }

    protected static String extractJhoveValue(String[] fieldPath, Property parentProperty,
                                              int fieldPathCurrentSegment) {

        //end of path - property not found
        if(null == fieldPath || null == parentProperty || fieldPathCurrentSegment < 0)
            return null;

        //last property in the path - return value
        if(fieldPathCurrentSegment >= fieldPath.length - 1)
            return getPropertyStringValue(parentProperty.getValue());

        //NisoImageMetadata
        if(PropertyType.NISOIMAGEMETADATA.equals(parentProperty.getType())){
            NisoImageMetadata nisoMetadata = (NisoImageMetadata) parentProperty.getValue();
            try {
                // getter reflection
                Object object = nisoMetadata.getClass().getMethod(
                        "get" + fieldPath[++fieldPathCurrentSegment]).invoke(nisoMetadata);
                return getPropertyStringValue(object);

            } catch (Exception e) {
                log.error(e.toString());
            }
            return null;
        }


        //TextMDMetadata
        if(PropertyType.TEXTMDMETADATA.equals(parentProperty.getType())){
            TextMDMetadata textMDMetadata = (TextMDMetadata) parentProperty.getValue();
            try {
                // getter reflection
                Object object = textMDMetadata.getClass().getMethod(
                        "get" + fieldPath[++fieldPathCurrentSegment]).invoke(textMDMetadata);
                return getPropertyStringValue(object);

            } catch (Exception e) {
                log.error(e.toString());
            }
            return null;
        }


        //AESAudioMedata
        if (PropertyType.AESAUDIOMETADATA.equals(parentProperty.getType())) {
            AESAudioMetadata aesMetadata = (AESAudioMetadata) parentProperty.getValue();
            try {
                // getter reflection
                Object object = aesMetadata.getClass().getMethod(
                        "get" + fieldPath[++fieldPathCurrentSegment]).invoke(aesMetadata);
                return getPropertyStringValue(object);
            } catch (Exception e) {
                log.error(e.toString());
            }
            return null;
        }

        //get next property from the path - continue recursion
        //try to extract the child property using get "getByName"
        //in case that the parent property is scalar and the still has child - using "getValue".
        fieldPathCurrentSegment++;
        String nextPropName=fieldPath[fieldPathCurrentSegment];
        Property nextProp = parentProperty.getByName(nextPropName);

        if (nextProp == null && parentProperty.getArity().equals(PropertyArity.SCALAR)&& parentProperty.getValue()!= null){

            nextProp = (Property)parentProperty.getValue();
        }


        return extractJhoveValue(fieldPath, nextProp, fieldPathCurrentSegment);
    }



    /********************************************************************
     * If object is of type collection/array - return content of object in
     * array form: [x, y, ..., z]
     * Otherwise return object content in simple string form (object.toString())
     ******************************************************************/
    private static String getPropertyStringValue(Object object)
    {

        if (object == null)
            return null;

        //int array
        if ((object instanceof int[]))
            return Arrays.toString((int[]) object);

        //long array
        if ((object instanceof long[]))
            return Arrays.toString((long[]) object);

        //double array
        if ((object instanceof double[]))
            return Arrays.toString((double[]) object);


        //Object array
        if ((object instanceof Object[])){

            Object[] arr = (Object[]) object;
            for(int i = 0; i< arr.length; i++){
                if (! (arr[i] instanceof Property)){
                    return Arrays.toString((Object[]) object);
                }
            }
            String propString = ColectionPropertyToString( object);
            return "["+propString.substring(0, propString.length()-2)+"]";
        }



        //Collection
        if ((object instanceof Collection))
        {
            Object[] arr = ((Collection)object).toArray();

            for(int i = 0; i< arr.length; i++){
                if (! (arr[i] instanceof Property)){
                    return Arrays.toString(arr);
                }
            }
            String propString = ColectionPropertyToString(((Collection)object).toArray());
            return "["+propString.substring(0, propString.length()-2)+"]";
        }

        //scalar
        String scalar = object.toString();

        try { //avoid -1 values
            return (Double.valueOf(scalar) == -1) ? null : scalar;
        } catch (Exception e) {}

        return scalar;
    }

    public String getAttributeByName(String attributeName) {

        // avoid null pointer exception in case property is null.
        if (repinfo.getProperty() == null) {
            return null;
        }

        try {
            String[] jhoveFieldPath = attributeName.split("\\.");
            String propertyValue = extractJhoveValue(jhoveFieldPath, repinfo.getByName(jhoveFieldPath[0]), 0);
            return propertyValue;
        } catch (Throwable e) {
            return null;
        }
    }

    public String getFormatName(){
        return repinfo.getFormat();
    }
    public String getFormatVersion() {
        return repinfo.getVersion();
    }
    public String getMimeType() {
        return repinfo.getMimeType();
    }

    public List<String> getExtractionErrors() {
        return errorMessages;
    }

    public List<String> getExtractionErrorIds() {
        return errorIds;
    }

    private static String ColectionPropertyToString(Object object){

        String res = "";

        //if object is [] object
        if (object  instanceof Object[]) {
            Object [] arr = (Object[]) object;
            for (int i = 0; i< arr.length ;i++){
                res+= ColectionPropertyToString((Property)arr[i]);
            }
        }
        // if object is a property of array
        else if (object  instanceof  Property && ((Property)object).getArity().equals(PropertyArity.ARRAY)){

            return ColectionPropertyToString(((Property)object).getValue());
        }

        // object is a property of scalar
        else if (object  instanceof  Property && ((Property)object).getArity().equals(PropertyArity.SCALAR)){

            Property prop = (Property)object;
            return prop.getName() + "=" + prop.getValue() + " ;";
        }

        return res;
    }

}
