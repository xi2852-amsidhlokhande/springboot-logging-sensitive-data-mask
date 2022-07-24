package com.amsidh.mvc.logger;

import com.fasterxml.jackson.core.JsonStreamContext;
import lombok.SneakyThrows;
import net.logstash.logback.mask.ValueMasker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

import static java.util.regex.Pattern.MULTILINE;

public class SensitiveMaskingPatternLayout implements ValueMasker {

    private Pattern partialMask;
    private Pattern fullMask;

    private Pattern compressMask;

    private final List<String> fullMaskPatterns = new ArrayList<>();
    private final List<String> partialMaskPatterns = new ArrayList<>();

    private final List<String> compressMaskPatterns = new ArrayList<>();
    private static final Integer LAST_PLAIN_TEXT_CHARS = 4;
    private static final Character MASKING_CHAR = '*';
    private static final String COLON = ":";

    public SensitiveMaskingPatternLayout() {
        initializeMaskingPattern();
    }

    private void initializeMaskingPattern() {
        this.partialMaskPatterns.add("[a-zA-Z]{5}\\d{4}[a-zA-Z]{1}"); //For PAN
        this.partialMaskPatterns.add("/[0-9]{15}/"); //For Account Number
        this.partialMaskPatterns.add("[0-9]{15}<"); //For Account Number in xml
        this.partialMaskPatterns.add("(?i)\\\"(?:accountNo|accountNumber|account|accNo)\\\"\\s*:\\s*\\\"(?:.*?)\\\""); //For account Number
        this.partialMaskPatterns.add("(?i)\\\"(?:cardNumber|cardNo|debitCardNumber)\\\"\\s*:\\s*\\\"(?:.*?)\\\""); //For account Number
        this.partialMaskPatterns.add("/[0-9]{12}[/]?"); //For Aadhaar
        this.partialMaskPatterns.add("(?i)\\\"(?:aadhaarNo|aadhaarNumber|aadhaar)\\\"\\s*:\\s*\\\"(?:.*?)\\\""); //For Aadhaar Number
        this.partialMaskPatterns.add("(?i)\\\"(?:mobile|mobileNo|mobileNumber|phoneNo|phoneNumber|phone|telephoneNo|residencePhone)\\\"\\s*:\\s*\\\"(?:.*?)\\\""); //For Mobile
        this.partialMask = Pattern.compile(String.join("|", this.partialMaskPatterns), MULTILINE);

        this.fullMaskPatterns.add("(?i)\\\"(?:dateOfBirth|dob|birthOrRegistrationDate)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)[\\\"]?(?:password|secret|X-IBM-Client-Secret)[\\\"]?\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)\\\"(?:custId|customerId|beneId|beneficiaryId)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)\\\"(?:fatherName|fathersName|fatherOrSpouseName|fatherSpouseName|fatherFirstName|fatherMiddleName|fatherLastName)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)\\\"(?:nomineeName|nominee_name|payeeName|guardianName)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)\\\"(?:name|firstName|lastName|middleName|fullName|aadhaarName|contactName|contactPerson)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)\\\"(?:customerFullName|customerFirstName|customerMiddleName|customerLastName|custName|customerName|panHolderName|embossName|secondaryAccountHolderName)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMaskPatterns.add("(?i)\\\"(?:motherName|motherMaidenName|mothersMaidenName|mothersName|motherFirstName|motherMiddleName|motherLastName)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.fullMask = Pattern.compile(String.join("|", this.fullMaskPatterns), MULTILINE);

        this.compressMaskPatterns.add("(?i)\\\"(?:largeData|aadhaarData|photo|image|file|data)\\\"\\s*:\\s*\\\"(?:.*?)\\\"");
        this.compressMask = Pattern.compile(String.join("|", this.compressMaskPatterns), MULTILINE);
    }

    @Override
    public Object mask(JsonStreamContext jsonStreamContext, Object object) {
        if (object instanceof CharSequence) {
            return maskMessage((String) object);
        }
        return object;
    }

    private String maskMessage(String message) {
        StringBuilder sb = new StringBuilder(message);

        Matcher partialMatcher = partialMask.matcher(sb);
        findAndReplace(partialMatcher, sb, true);
        Matcher fullMatcher = fullMask.matcher(sb);
        findAndReplace(fullMatcher, sb, false);

        //Compress Data Masking
        Matcher compressDataMatcher = compressMask.matcher(sb);
        compressData(compressDataMatcher, sb);

        return sb.toString();
    }

    private void findAndReplace(Matcher matcher, StringBuilder sb, boolean matcherType) {
        while (matcher.find()) {
            IntStream.rangeClosed(0, matcher.groupCount()).forEach(group -> {
                if (matcher.group(group) != null) {
                    int start = matcher.start(group);
                    int end = matcher.end(group);
                    end = (end > LAST_PLAIN_TEXT_CHARS && matcherType) ? (end - LAST_PLAIN_TEXT_CHARS) : end;

                    String result = sb.substring(start, end);

                    start = result.contains(COLON) ? sb.toString().indexOf(COLON, start) + 1 : start;

                    IntStream.rangeClosed(start, end).forEach(index -> sb.setCharAt(index, MASKING_CHAR));
                }
            });
        }
    }

    private void compressData(Matcher matcher, StringBuilder sb) {
        while (matcher.find()) {
            IntStream.rangeClosed(0, matcher.groupCount()).forEach(group -> {
                if (matcher.group(group) != null) {
                    int start = matcher.start(group);
                    int end = matcher.end(group);
                    String result = sb.substring(start, end);
                    start = result.contains(COLON) ? sb.toString().indexOf(COLON, start) + 1 : start;
                    String data = sb.substring(start, end);
                    sb.replace(start, end, compressString(data));
                }
            });
        }
    }

    @SneakyThrows
    private String compressString(String data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(data.getBytes());
        gzip.close();
        return out.toString("ISO-8859-1");
    }

}
