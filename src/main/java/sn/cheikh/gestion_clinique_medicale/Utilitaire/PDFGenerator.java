package sn.cheikh.gestion_clinique_medicale.Utilitaire;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Facture;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    private static final Font FONT_TITRE    = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font FONT_SECTION  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font FONT_NORMAL   = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_SMALL    = new Font(Font.FontFamily.HELVETICA, 9,  Font.ITALIC, BaseColor.GRAY);
    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private PDFGenerator() {}


    public static void genererOrdonnance(Consultation consultation, String cheminFichier) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            ajouterEnTete(document, "ORDONNANCE MEDICALE");
            ajouterLigneHorizontale(document);

            // Infos patient
            document.add(new Paragraph("Informations du patient", FONT_SECTION));
            document.add(new Paragraph("Nom complet : " + consultation.getPatient().getNomComplet(), FONT_NORMAL));
            document.add(new Paragraph("Date        : " + consultation.getDateConsultation().format(FORMAT_DATE), FONT_NORMAL));
            document.add(new Paragraph("Médecin     : Dr. " + consultation.getMedecin().getNomComplet(), FONT_NORMAL));
            document.add(Chunk.NEWLINE);

            ajouterLigneHorizontale(document);

            document.add(new Paragraph("Diagnostic", FONT_SECTION));
            document.add(new Paragraph(
                    consultation.getDiagnostic() != null ? consultation.getDiagnostic() : "-",
                    FONT_NORMAL));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Prescription / Traitement", FONT_SECTION));
            document.add(new Paragraph(
                    consultation.getPrescription() != null ? consultation.getPrescription() : "-",
                    FONT_NORMAL));
            document.add(Chunk.NEWLINE);

            ajouterLigneHorizontale(document);
            ajouterPiedDePage(document, "Dr. " + consultation.getMedecin().getNomComplet());

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération ordonnance PDF : " + e.getMessage(), e);
        }
    }

    public static void genererFacture(Facture facture, String cheminFichier) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            ajouterEnTete(document, "FACTURE");
            ajouterLigneHorizontale(document);

            Consultation consultation = facture.getConsultation();

            // Infos patient
            document.add(new Paragraph("Informations du patient", FONT_SECTION));
            document.add(new Paragraph("Nom complet : " + consultation.getPatient().getNomComplet(), FONT_NORMAL));
            document.add(new Paragraph("Date        : " + facture.getDateFacture().format(FORMAT_DATE), FONT_NORMAL));
            document.add(new Paragraph("Médecin     : Dr. " + consultation.getMedecin().getNomComplet(), FONT_NORMAL));
            document.add(Chunk.NEWLINE);

            ajouterLigneHorizontale(document);

            document.add(new Paragraph("Detail de la facturation", FONT_SECTION));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1});

            ajouterCelluleTableau(table, "Designation",      true);
            ajouterCelluleTableau(table, "Montant (FCFA)",   true);
            ajouterCelluleTableau(table, "Consultation medicale", false);
            ajouterCelluleTableau(table, String.format("%.0f", facture.getMontantTotal()), false);

            document.add(table);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Mode de paiement : " + facture.getModePaiement(), FONT_NORMAL));
            document.add(new Paragraph("Statut           : " + facture.getStatutPaiement(), FONT_NORMAL));
            document.add(new Paragraph("TOTAL            : " + String.format("%.0f FCFA", facture.getMontantTotal()),
                    new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, BaseColor.DARK_GRAY)));

            ajouterLigneHorizontale(document);
            ajouterPiedDePage(document, "Clinique Medicale");

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation facture PDF : " + e.getMessage(), e);
        }
    }

    private static void ajouterEnTete(Document document, String titre) throws DocumentException {
        Paragraph nomClinique = new Paragraph("CLINIQUE MÉDICALE", FONT_TITRE);
        nomClinique.setAlignment(Element.ALIGN_CENTER);
        document.add(nomClinique);

        Paragraph sousTitre = new Paragraph(titre, FONT_SECTION);
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        document.add(sousTitre);
        document.add(Chunk.NEWLINE);
    }

    private static void ajouterLigneHorizontale(Document document) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private static void ajouterCelluleTableau(PdfPTable table, String texte, boolean estEntete) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, estEntete ? FONT_SECTION : FONT_NORMAL));
        cell.setPadding(6);
        if (estEntete) cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static void ajouterPiedDePage(Document document, String signataire) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph signature = new Paragraph("Signé par : " + signataire, FONT_SMALL);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);

        Paragraph footer = new Paragraph("Document genere automatiquement — Clinique Médicale", FONT_SMALL);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}