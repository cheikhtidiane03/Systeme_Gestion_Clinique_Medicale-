package sn.cheikh.gestion_clinique_medicale.Utilitaire;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import sn.cheikh.gestion_clinique_medicale.model.Consultation;
import sn.cheikh.gestion_clinique_medicale.model.Facture;
import sn.cheikh.gestion_clinique_medicale.model.RendezVous;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFGenerator {

    private static final String DOSSIER_BASE        = "pdfs";
    private static final String DOSSIER_ORDONNANCES  = DOSSIER_BASE + File.separator + "ordonnances";
    private static final String DOSSIER_FACTURES     = DOSSIER_BASE + File.separator + "factures";
    private static final String DOSSIER_RAPPORTS     = DOSSIER_BASE + File.separator + "rapports";

    private static final DateTimeFormatter FORMAT_DATE    = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMAT_JOUR    = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_FICHIER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final Font FONT_TITRE    = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,   BaseColor.DARK_GRAY);
    private static final Font FONT_SECTION  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,   BaseColor.DARK_GRAY);
    private static final Font FONT_NORMAL   = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_SMALL    = new Font(Font.FontFamily.HELVETICA,  9, Font.ITALIC, BaseColor.GRAY);
    private static final Font FONT_TOTAL    = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD,   new BaseColor(79, 70, 229));

    private PDFGenerator() {}

    private static void creerDossiers() {
        new File(DOSSIER_ORDONNANCES).mkdirs();
        new File(DOSSIER_FACTURES).mkdirs();
        new File(DOSSIER_RAPPORTS).mkdirs();
    }

    private static String cheminOrdonnance(Consultation c) {
        String patient = c.getPatient().getNom().replaceAll("\\s+", "_");
        return DOSSIER_ORDONNANCES + File.separator
                + "ordonnance_" + patient + "_" + LocalDateTime.now().format(FORMAT_FICHIER) + ".pdf";
    }

    private static String cheminFacture(Facture f) {
        String patient = f.getConsultation().getPatient().getNom().replaceAll("\\s+", "_");
        return DOSSIER_FACTURES + File.separator
                + "facture_" + patient + "_" + LocalDateTime.now().format(FORMAT_FICHIER) + ".pdf";
    }

    private static String cheminRapport(LocalDate date) {
        return DOSSIER_RAPPORTS + File.separator
                + "rapport_journalier_" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
    }

    // ══════════════════════════════════════════════════════════
    //  ORDONNANCE
    // ══════════════════════════════════════════════════════════
    public static String genererOrdonnance(Consultation consultation) {
        creerDossiers();
        String chemin = cheminOrdonnance(consultation);
        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, new FileOutputStream(chemin));
            doc.open();

            ajouterEnTete(doc, "ORDONNANCE MÉDICALE");
            ajouterLigne(doc);

            doc.add(new Paragraph("Informations du patient", FONT_SECTION));
            doc.add(new Paragraph("Nom complet : " + consultation.getPatient().getNomComplet(), FONT_NORMAL));
            doc.add(new Paragraph("Date        : " + consultation.getDateConsultation().format(FORMAT_DATE), FONT_NORMAL));
            doc.add(new Paragraph("Médecin     : Dr. " + consultation.getMedecin().getNomComplet(), FONT_NORMAL));
            doc.add(Chunk.NEWLINE);

            ajouterLigne(doc);

            doc.add(new Paragraph("Diagnostic", FONT_SECTION));
            doc.add(new Paragraph(nvl(consultation.getDiagnostic()), FONT_NORMAL));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Observations", FONT_SECTION));
            doc.add(new Paragraph(nvl(consultation.getObservations()), FONT_NORMAL));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Prescription / Traitement", FONT_SECTION));
            doc.add(new Paragraph(nvl(consultation.getPrescription()), FONT_NORMAL));
            doc.add(Chunk.NEWLINE);

            ajouterLigne(doc);
            ajouterPiedDePage(doc, "Dr. " + consultation.getMedecin().getNomComplet());
            doc.close();
            return chemin;
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération ordonnance PDF : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  FACTURE
    // ══════════════════════════════════════════════════════════
    public static String genererFacture(Facture facture) {
        creerDossiers();
        String chemin = cheminFacture(facture);
        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, new FileOutputStream(chemin));
            doc.open();

            ajouterEnTete(doc, "FACTURE");
            ajouterLigne(doc);

            Consultation c = facture.getConsultation();
            doc.add(new Paragraph("Informations du patient", FONT_SECTION));
            doc.add(new Paragraph("Nom complet : " + c.getPatient().getNomComplet(), FONT_NORMAL));
            doc.add(new Paragraph("Date        : " + facture.getDateFacture().format(FORMAT_DATE), FONT_NORMAL));
            doc.add(new Paragraph("Médecin     : Dr. " + c.getMedecin().getNomComplet(), FONT_NORMAL));
            doc.add(Chunk.NEWLINE);

            ajouterLigne(doc);

            doc.add(new Paragraph("Détail de la facturation", FONT_SECTION));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1});
            ajouterCellule(table, "Désignation",          true);
            ajouterCellule(table, "Montant (FCFA)",        true);
            ajouterCellule(table, "Consultation médicale", false);
            ajouterCellule(table, String.format("%.0f", facture.getMontantTotal()), false);
            doc.add(table);
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Mode de paiement : " + nvl(facture.getModePaiement()), FONT_NORMAL));
            doc.add(new Paragraph("Statut           : " + facture.getStatutPaiement().name(), FONT_NORMAL));
            doc.add(new Paragraph("TOTAL            : " + String.format("%.0f FCFA", facture.getMontantTotal()), FONT_TOTAL));

            ajouterLigne(doc);
            ajouterPiedDePage(doc, "Clinique Médicale");
            doc.close();
            return chemin;
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération facture PDF : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  RAPPORT JOURNALIER
    // ══════════════════════════════════════════════════════════
    public static String genererRapportJournalier(
            LocalDate date,
            List<RendezVous>   rdvJour,
            List<Consultation> consultationsJour,
            List<Facture>      facturesJour) {

        creerDossiers();
        String chemin = cheminRapport(date);
        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, new FileOutputStream(chemin));
            doc.open();

            // ── En-tête ──
            ajouterEnTete(doc, "RAPPORT JOURNALIER");
            Paragraph datePar = new Paragraph("Date : " + date.format(FORMAT_JOUR), FONT_NORMAL);
            datePar.setAlignment(Element.ALIGN_CENTER);
            doc.add(datePar);
            doc.add(Chunk.NEWLINE);
            ajouterLigne(doc);

            // ── Résumé ──
            double totalEncaisse = facturesJour.stream()
                    .filter(f -> f.getStatutPaiement().name().equals("PAYE"))
                    .mapToDouble(Facture::getMontantTotal)
                    .sum();
            double totalFacture = facturesJour.stream()
                    .mapToDouble(Facture::getMontantTotal)
                    .sum();

            doc.add(new Paragraph("RÉSUMÉ DU JOUR", FONT_SECTION));
            doc.add(Chunk.NEWLINE);

            PdfPTable resume = new PdfPTable(2);
            resume.setWidthPercentage(60);
            resume.setHorizontalAlignment(Element.ALIGN_LEFT);
            resume.setWidths(new float[]{3, 1});
            ajouterCellule(resume, "Rendez-vous programmés",    false);
            ajouterCellule(resume, String.valueOf(rdvJour.size()), false);
            ajouterCellule(resume, "Consultations effectuées",  false);
            ajouterCellule(resume, String.valueOf(consultationsJour.size()), false);
            ajouterCellule(resume, "Factures émises",           false);
            ajouterCellule(resume, String.valueOf(facturesJour.size()), false);
            ajouterCellule(resume, "Montant total facturé",     false);
            ajouterCellule(resume, String.format("%.0f FCFA", totalFacture), false);
            ajouterCellule(resume, "Montant encaissé",          false);
            ajouterCellule(resume, String.format("%.0f FCFA", totalEncaisse), false);
            doc.add(resume);
            doc.add(Chunk.NEWLINE);
            ajouterLigne(doc);

            // ── Rendez-vous ──
            doc.add(new Paragraph("RENDEZ-VOUS DU JOUR (" + rdvJour.size() + ")", FONT_SECTION));
            doc.add(Chunk.NEWLINE);
            if (rdvJour.isEmpty()) {
                doc.add(new Paragraph("Aucun rendez-vous ce jour.", FONT_NORMAL));
            } else {
                PdfPTable tRdv = new PdfPTable(4);
                tRdv.setWidthPercentage(100);
                tRdv.setWidths(new float[]{2.5f, 2.5f, 1.2f, 1.5f});
                ajouterCellule(tRdv, "Patient",  true);
                ajouterCellule(tRdv, "Médecin",  true);
                ajouterCellule(tRdv, "Heure",    true);
                ajouterCellule(tRdv, "Statut",   true);
                for (RendezVous r : rdvJour) {
                    ajouterCellule(tRdv, r.getPatient().getNomComplet(), false);
                    ajouterCellule(tRdv, "Dr. " + r.getMedecin().getNomComplet(), false);
                    ajouterCellule(tRdv, r.getDateHeure().format(DateTimeFormatter.ofPattern("HH:mm")), false);
                    ajouterCellule(tRdv, r.getStatut().name(), false);
                }
                doc.add(tRdv);
            }
            doc.add(Chunk.NEWLINE);
            ajouterLigne(doc);

            // ── Consultations ──
            doc.add(new Paragraph("CONSULTATIONS DU JOUR (" + consultationsJour.size() + ")", FONT_SECTION));
            doc.add(Chunk.NEWLINE);
            if (consultationsJour.isEmpty()) {
                doc.add(new Paragraph("Aucune consultation ce jour.", FONT_NORMAL));
            } else {
                PdfPTable tConsult = new PdfPTable(3);
                tConsult.setWidthPercentage(100);
                tConsult.setWidths(new float[]{2.5f, 2.5f, 4f});
                ajouterCellule(tConsult, "Patient",    true);
                ajouterCellule(tConsult, "Médecin",    true);
                ajouterCellule(tConsult, "Diagnostic", true);
                for (Consultation c : consultationsJour) {
                    ajouterCellule(tConsult, c.getPatient().getNomComplet(), false);
                    ajouterCellule(tConsult, "Dr. " + c.getMedecin().getNomComplet(), false);
                    ajouterCellule(tConsult, nvl(c.getDiagnostic()), false);
                }
                doc.add(tConsult);
            }
            doc.add(Chunk.NEWLINE);
            ajouterLigne(doc);

            // ── Factures ──
            doc.add(new Paragraph("FACTURES DU JOUR (" + facturesJour.size() + ")", FONT_SECTION));
            doc.add(Chunk.NEWLINE);
            if (facturesJour.isEmpty()) {
                doc.add(new Paragraph("Aucune facture ce jour.", FONT_NORMAL));
            } else {
                PdfPTable tFact = new PdfPTable(4);
                tFact.setWidthPercentage(100);
                tFact.setWidths(new float[]{2.5f, 1.8f, 1.8f, 1.5f});
                ajouterCellule(tFact, "Patient",         true);
                ajouterCellule(tFact, "Montant (FCFA)",  true);
                ajouterCellule(tFact, "Mode",            true);
                ajouterCellule(tFact, "Statut",          true);
                for (Facture f : facturesJour) {
                    ajouterCellule(tFact, f.getConsultation().getPatient().getNomComplet(), false);
                    ajouterCellule(tFact, String.format("%.0f", f.getMontantTotal()), false);
                    ajouterCellule(tFact, nvl(f.getModePaiement()), false);
                    ajouterCellule(tFact, f.getStatutPaiement().name(), false);
                }
                doc.add(tFact);
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(
                        "TOTAL ENCAISSÉ : " + String.format("%.0f FCFA", totalEncaisse), FONT_TOTAL));
            }

            ajouterLigne(doc);
            ajouterPiedDePage(doc, "Direction — Clinique Médicale");
            doc.close();
            return chemin;
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération rapport journalier : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  MÉTHODES PRIVÉES
    // ══════════════════════════════════════════════════════════
    private static void ajouterEnTete(Document doc, String titre) throws DocumentException {
        Paragraph nom = new Paragraph("CLINIQUE MÉDICALE", FONT_TITRE);
        nom.setAlignment(Element.ALIGN_CENTER);
        doc.add(nom);
        Paragraph t = new Paragraph(titre, FONT_SECTION);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);
        doc.add(Chunk.NEWLINE);
    }

    private static void ajouterLigne(Document doc) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private static void ajouterCellule(PdfPTable table, String texte, boolean entete) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, entete ? FONT_SECTION : FONT_NORMAL));
        cell.setPadding(6);
        if (entete) cell.setBackgroundColor(new BaseColor(238, 242, 255));
        table.addCell(cell);
    }

    private static void ajouterPiedDePage(Document doc, String signataire) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph sig = new Paragraph("Signé par : " + signataire, FONT_SMALL);
        sig.setAlignment(Element.ALIGN_RIGHT);
        doc.add(sig);
        Paragraph footer = new Paragraph(
                "Document généré automatiquement — Clinique Médicale", FONT_SMALL);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    private static String nvl(String s) {
        return (s != null && !s.isBlank()) ? s : "—";
    }
}