
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'dart:typed_data';

class ImprimerImagePage{
  // Fonction pour générer un PDF contenant une image
  static Future<void> generatePdfWithImage(Uint8List image ) async {
    final pdf = pw.Document();
    final Uint8List imageBytes = image;

    // Convertir en image utilisable par le package pdf
    final pw.MemoryImage pdfImage = pw.MemoryImage(imageBytes);
    pdf.addPage(
      pw.Page(
        build: (pw.Context context) {
          return pw.Center(
            child: pw.Image(pdfImage),
          );
        },
      ),
    );
    final pdfData =  await pdf.save();
    await Printing.layoutPdf(onLayout: (format) => pdfData);
  }

}