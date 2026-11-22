package pe.edu.pucp.klam.reportes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.pucp.klam.db.DBManager;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@WebServlet(name = "ReporteDespachoConsumo", urlPatterns = { "/reportes/despacho-consumo" })
public class ReporteDespachoConsumo extends HttpServlet {
    private final String NOMBRE_REPORTE = "reportes/Reporte_DespachoConsumo.jasper";
    private final String SUB_REPORTE = "reportes/Reporte_DetalleDespachoConsumo.jasper";
    private final String NOMBRE_LOGO = "imagenes/bioklam-logo.png";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/pdf");

        InputStream reporte = getClass().getClassLoader().getResourceAsStream(NOMBRE_REPORTE);
        if (reporte == null) {
            throw new FileNotFoundException("No se encontró el reporte maestro: " + NOMBRE_REPORTE);
        }

        Map<String, Object> parametros = new HashMap<>();

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Debe especificar el parámetro 'id' de la cirugía.");
            return;
        }

        int idCirugia = Integer.parseInt(idParam.trim());
        parametros.put("id_cirugia", idCirugia);
        parametros.put("subreporte", SUB_REPORTE);

        InputStream logoStream = getClass().getClassLoader().getResourceAsStream(NOMBRE_LOGO);
        if (logoStream != null) {
            Image image = ImageIO.read(logoStream);
            parametros.put("logo", image);
        }

        try (Connection conn = DBManager.getInstance().getConnection()) {
            JasperPrint jp = JasperFillManager.fillReport(reporte, parametros, conn);
            JasperExportManager.exportReportToPdfStream(jp, response.getOutputStream());
        } catch (SQLException | JRException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar el reporte de despacho y consumo: " + ex.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    public String getServletInfo() {
        return "Servlet para generar el reporte de despacho y consumo por cirugía";
    }
}
