package com.resumeanalyzer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SampleGeneratorTest {

    @Test
    public void generateSampleResumes() throws IOException {
        File samplesDir = new File("../samples");
        if (!samplesDir.exists()) {
            samplesDir.mkdirs();
        }

        // 1. Generate John_Doe_FullStack_Resume.pdf
        File pdfFile = new File(samplesDir, "John_Doe_FullStack_Resume.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                stream.beginText();
                stream.setFont(bold, 18);
                stream.newLineAtOffset(50, 750);
                stream.showText("JOHN DOE");

                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -18);
                stream.showText("Email: john.doe@example.com | Phone: +91 9876543210 | Location: Bengaluru, India");

                stream.newLineAtOffset(0, -14);
                stream.showText("GitHub: github.com/johndoe | LinkedIn: linkedin.com/in/johndoe");

                // Summary
                stream.setFont(bold, 12);
                stream.newLineAtOffset(0, -25);
                stream.showText("PROFESSIONAL SUMMARY");
                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -15);
                stream.showText("Dedicated Software Engineer with 2+ years of experience designing scalable microservices");
                stream.newLineAtOffset(0, -12);
                stream.showText("in Java and Spring Boot. Strong background in React, REST APIs, PostgreSQL, and Docker.");

                // Skills
                stream.setFont(bold, 12);
                stream.newLineAtOffset(0, -22);
                stream.showText("TECHNICAL SKILLS");
                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -15);
                stream.showText("Programming Languages: Java, Python, JavaScript, TypeScript, SQL");
                stream.newLineAtOffset(0, -12);
                stream.showText("Frameworks & Tools: Spring Boot, React.js, Hibernate, Redux, Node.js, Express");
                stream.newLineAtOffset(0, -12);
                stream.showText("Databases & Cloud: MySQL, PostgreSQL, MongoDB, Docker, Kubernetes, AWS, Git");

                // Experience
                stream.setFont(bold, 12);
                stream.newLineAtOffset(0, -22);
                stream.showText("WORK EXPERIENCE");
                stream.setFont(bold, 10);
                stream.newLineAtOffset(0, -15);
                stream.showText("Backend Software Developer Intern - TechCorp Solutions (2023 - 2024)");
                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -12);
                stream.showText("- Architected RESTful microservices using Spring Boot and Hibernate JPA.");
                stream.newLineAtOffset(0, -12);
                stream.showText("- Optimized MySQL queries reducing database response latency by 35%.");
                stream.newLineAtOffset(0, -12);
                stream.showText("- Built containerized deployments using Docker and GitHub Actions CI/CD.");

                // Projects
                stream.setFont(bold, 12);
                stream.newLineAtOffset(0, -22);
                stream.showText("NOTABLE PROJECTS");
                stream.setFont(bold, 10);
                stream.newLineAtOffset(0, -15);
                stream.showText("E-Commerce Microservices Platform");
                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -12);
                stream.showText("- Developed reactive catalog service with Spring Boot, Redis caching, and Kafka.");
                stream.newLineAtOffset(0, -12);
                stream.showText("- Built modern customer checkout frontend using React.js and Tailwind CSS.");

                // Education
                stream.setFont(bold, 12);
                stream.newLineAtOffset(0, -22);
                stream.showText("EDUCATION");
                stream.setFont(bold, 10);
                stream.newLineAtOffset(0, -15);
                stream.showText("Bachelor of Technology in Computer Science and Engineering");
                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -12);
                stream.showText("National Institute of Technology | Graduation: 2024 | CGPA: 8.8 / 10.0");

                // Certifications
                stream.setFont(bold, 12);
                stream.newLineAtOffset(0, -22);
                stream.showText("CERTIFICATIONS");
                stream.setFont(regular, 10);
                stream.newLineAtOffset(0, -15);
                stream.showText("- Oracle Certified Professional: Java SE 17 Developer");
                stream.newLineAtOffset(0, -12);
                stream.showText("- AWS Certified Cloud Practitioner");

                stream.endText();
            }

            doc.save(pdfFile);
            System.out.println("Generated PDF at: " + pdfFile.getAbsolutePath());
        }

        // 2. Generate Jane_Smith_Junior_Developer.docx
        File docxFile = new File(samplesDir, "Jane_Smith_Junior_Developer.docx");
        try (XWPFDocument docx = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(docxFile)) {

            XWPFParagraph p1 = docx.createParagraph();
            XWPFRun r1 = p1.createRun();
            r1.setBold(true);
            r1.setFontSize(18);
            r1.setText("JANE SMITH");

            XWPFParagraph p2 = docx.createParagraph();
            XWPFRun r2 = p2.createRun();
            r2.setText("Email: jane.smith@example.com | Phone: +91 9123456780 | Location: Pune, India");

            XWPFParagraph p3 = docx.createParagraph();
            XWPFRun r3 = p3.createRun();
            r3.setBold(true);
            r3.setFontSize(14);
            r3.setText("TECHNICAL SKILLS");

            XWPFParagraph p4 = docx.createParagraph();
            XWPFRun r4 = p4.createRun();
            r4.setText("Java, Spring Boot, MySQL, REST API, Git, Docker, HTML, CSS, JavaScript");

            XWPFParagraph p5 = docx.createParagraph();
            XWPFRun r5 = p5.createRun();
            r5.setBold(true);
            r5.setFontSize(14);
            r5.setText("EDUCATION");

            XWPFParagraph p6 = docx.createParagraph();
            XWPFRun r6 = p6.createRun();
            r6.setText("B.E. in Information Technology, Pune University (2025) - CGPA: 8.2");

            XWPFParagraph p7 = docx.createParagraph();
            XWPFRun r7 = p7.createRun();
            r7.setBold(true);
            r7.setFontSize(14);
            r7.setText("PROJECTS");

            XWPFParagraph p8 = docx.createParagraph();
            XWPFRun r8 = p8.createRun();
            r8.setText("Library Management System: Built Spring Boot REST APIs with MySQL database and Postman testing.");

            docx.write(out);
            System.out.println("Generated DOCX at: " + docxFile.getAbsolutePath());
        }
    }
}
