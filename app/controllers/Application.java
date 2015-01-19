package controllers;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.json.JSONException;
import play.*;
import play.mvc.*;

import java.io.*;
import java.util.*;

import models.*;
import service.fillPDF;

public class Application extends Controller {

    public static void index()  {
        render();
    }

    public static void mesclarPDF(String urlObj, String urlPDF, Boolean disable) throws JSONException, COSVisitorException, IOException {
        OutputStream out = new fillPDF(disable).mesclarDecode(urlObj, urlPDF);
        InputStream decodedInput = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());

        response.setContentTypeIfNotSet("application/pdf");
        renderBinary(decodedInput);
    }

    public static void mesclarJsonPdf(String jsonObj, String urlPDF, Boolean disable) throws JSONException, COSVisitorException, IOException {
        OutputStream out = new fillPDF(disable).mesclarJsonPdf(jsonObj, urlPDF);
        InputStream decodedInput = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());

        response.setContentTypeIfNotSet("application/pdf");
        renderBinary(decodedInput);
    }
}