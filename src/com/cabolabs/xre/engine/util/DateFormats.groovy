package com.cabolabs.xre.engine.util

import java.text.Format
import java.text.SimpleDateFormat

class DateFormats {

   // http://en.wikipedia.org/wiki/ISO_8601
   // http://dotat.at/tmp/ISO_8601-2004_E.pdf
   
   // HH es hora en 0-23
   
   // Basic ISO 8601 format
   final static String BASIC_DATE_PATTERN               = 'yyyyMMdd'
   
   final static String BASIC_DATETIME_MIN_PATTERN       = 'yyyyMMddHHmm' // Sin segundos
   final static String BASIC_DATETIME_MIN_TZ_PATTERN    = 'yyyyMMddHHmmZ' // Sin segundos con timezone
   final static String BASIC_DATETIME_PATTERN           = 'yyyyMMddHHmmss'
   final static String BASIC_DATETIME_TZ_PATTERN        = 'yyyyMMddHHmmssZ'
   
   // Basic ISO 8601 format con la T
   final static String BASIC_DATETIME_T_MIN_PATTERN     = "yyyyMMdd'T'HHmm" // Sin segundos
   final static String BASIC_DATETIME_T_MIN_TZ_PATTERN  = "yyyyMMdd'T'HHmmZ" // Sin segundos con timezone
   final static String BASIC_DATETIME_T_PATTERN         = "yyyyMMdd'T'HHmmss"
   final static String BASIC_DATETIME_T_TZ_PATTERN      = "yyyyMMdd'T'HHmmssZ"
   
   // Extended ISO 8601 format
   final static String EXTENDED_DATE_PATTERN            = 'yyyy-MM-dd'
   final static String EXTENDED_DATETIME_MIN_PATTERN    = "yyyy-MM-dd'T'HH:mm" // Sin segundos
   final static String EXTENDED_DATETIME_MIN_TZ_PATTERN = "yyyy-MM-dd'T'HH:mmZ" // Sin segundos con timezone
   final static String EXTENDED_DATETIME_PATTERN        = "yyyy-MM-dd'T'HH:mm:ss"
   final static String EXTENDED_DATETIME_TZ_PATTERN     = "yyyy-MM-dd'T'HH:mm:ssZ"
   
   
   final static List formats = [
      BASIC_DATETIME_TZ_PATTERN,        // yyyyMMddHHmmssZ
      BASIC_DATETIME_MIN_TZ_PATTERN,    // yyyyMMddHHmmZ
      BASIC_DATETIME_PATTERN,           // yyyyMMddHHmmss (se considera UTC)
      BASIC_DATETIME_MIN_PATTERN,       // yyyyMMddHHmm
      
      BASIC_DATETIME_T_TZ_PATTERN,      // yyyyMMdd'T'HHmmssZ
      BASIC_DATETIME_T_MIN_TZ_PATTERN,  // yyyyMMdd'T'HHmmZ // Sin segundos con timezone
      BASIC_DATETIME_T_PATTERN,         // yyyyMMdd'T'HHmmss
      BASIC_DATETIME_T_MIN_PATTERN,     // yyyyMMdd'T'HHmm // Sin segundos
      
      BASIC_DATE_PATTERN,               // yyyyMMdd
      
      // Extended ISO 8601 format
      EXTENDED_DATETIME_TZ_PATTERN,     // yyyy-MM-dd'T'HH:mm:ssZ
      EXTENDED_DATETIME_MIN_TZ_PATTERN, // yyyy-MM-dd'T'HH:mmZ // Sin segundos con timezone
      EXTENDED_DATETIME_PATTERN,        // yyyy-MM-dd'T'HH:mm:ss
      EXTENDED_DATETIME_MIN_PATTERN,    // yyyy-MM-dd'T'HH:mm // Sin segundos
      EXTENDED_DATE_PATTERN             // yyyy-MM-dd
   ]
   
   final static List regpatterns = [
      /^(\d\d\d\d\d\d\d\d\d\d\d\d\d\d)([+-]\d\d\d\d)$/, //yyyyMMddHHmmss+-zzzz
      /^(\d\d\d\d\d\d\d\d\d\d\d\d)([+-]\d\d\d\d)$/, //yyyyMMddHHmm+-zzzz
      /^(\d\d\d\d\d\d\d\d\d\d\d\d\d\d)$/, //yyyyMMddHHmmss
      /^(\d\d\d\d\d\d\d\d\d\d\d\d)$/, //yyyyMMddHHmm
      
      /^(\d\d\d\d\d\d\d\dT\d\d\d\d\d\d)([+-]\d\d\d\d)$/, //yyyyMMddTHHmmss+-zzzz
      /^(\d\d\d\d\d\d\d\dT\d\d\d\d)([+-]\d\d\d\d)$/, //yyyyMMddTHHmm+-zzzz
      /^(\d\d\d\d\d\d\d\dT\d\d\d\d\d\d)$/, //yyyyMMddTHHmmss
      /^(\d\d\d\d\d\d\d\dT\d\d\d\d)$/, //yyyyMMddTHHmm
      
      /^(\d\d\d\d\d\d\d\d)$/, //yyyyMMdd
      
      /^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d)([+-]\d\d\d\d)$/, //yyyy-MM-ddTHH:mm:ss+-zzzz
      /^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d)([+-]\d\d\d\d)$/, //yyyy-MM-ddTHH:mm+-zzzz
      /^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d)$/, //yyyy-MM-ddTHH:mm:ss
      /^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d)$/, //yyyy-MM-ddTHH:mm
      
      /^(\d\d\d\d-\d\d-\d\d)$/ //yyyy-MM-dd
   ]
   
   
   static String dateFormat(Date date, String toFormat)
   {
      Format formatter = new SimpleDateFormat(toFormat)
      return formatter.format(date)
   }
   
   /**
    * Quien use este metodo conoce el formato que tiene la fecha, y se utiliza para luego llamar a dateFormat con esa fecha.
    * @param date
    * @param format
    * @return
    */
   static Date parse(String date, String format)
   {
      Format formatter = new SimpleDateFormat(format)
      return (Date)formatter.parse(date)
   }
   
   /**
    * Parsea la fecha probando alternativas de formato. Es decir, quien llama no sabe el formato.
    * @param date
    * @return
    */
   static Date parse(String date)
   {
      String format = findFormat(date)
      Format formatter = new SimpleDateFormat(format)
      return (Date)formatter.parse(date)
   }
   
   /**
    * Metodo rapido que utiliza los metodos parse y dateFormat internamente.
    * @param date
    * @param currentFormat
    * @param toFormat
    * @return
    */
   static String dateFormat(String date, String currentFormat, String toFormat)
   {
      Date d = parse(date, currentFormat)
      return dateFormat(d, toFormat)
   }
   
   static String findFormat(String date)
   {
      //Date d
      String currentFormat
      //String timezone
      for(String pattern in regpatterns)
      {
         def m = ( date =~ pattern )
         //println "m: " + m
         if (m)
         {
            //println m[0].size() // 3 si tiene timezone [20120710160933-0400, 20120710160933, -0400]
            //println m[0]
            
            //timezone = "UTC"
            //if (m[0].size() == 3) timezone = "GMT" + m[0][2] // UTC no funka aca
            
            currentFormat = formats[ regpatterns.indexOf(pattern) ] // Formato de fecha correspondiente a la pattern
            
            return currentFormat
            
            //d = parse(date, currentFormat) // si falla va al catch
            //println "Match $currentFormat $timezone"
            
            
            //SimpleDateFormat formatter = new SimpleDateFormat(toFormat)
            //formatter.setTimeZone(TimeZone.getTimeZone(timezone))
            //return formatter.format(d)
         }
      }
      
      throw new Exception("Date format not found for '$date'")
   }
   
   /**
    * Prueba por diferentes currentFormat hasta encontrar uno que se cumpla.
    * @param date
    * @param toFormat
    * @return
    */
   static String dateFormatWithAlternatives(String date, String toFormat)
   {
      // Verifica primero los mas completos, sino matchea con uno que no deberia,
      // por ejemplo yyyyMMdd matchea con 201207101609-0400
      // PROBLEMA: yyyyMMddhhmmss matchea con 201207101609-0400 (no tiene seg y si tiene TZ)
      
      
      // Por el problema del match de date, lo hago con regex
      /*
      List patterns = [
         "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d([+-]\\d\\d\\d\\d)", //yyyyMMddhhmmss+-zzzz
         "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d([+-]\\d\\d\\d\\d)", //yyyyMMddhhmm+-zzzz
         "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d", //yyyyMMddhhmmss
         "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d", //yyyyMMddhhmm
         "\\d\\d\\d\\d\\d\\d\\d\\d" //yyyyMMdd
      ]
      */

      Date d
      String currentFormat
      String timezone
      for(String pattern in regpatterns)
      {
         def m = ( date =~ pattern )
         //def m = (~pattern).matcher(date)
         //println "m: " + m
         if (m)
         {
            //println m[0].size() // 3 si tiene timezone [20120710160933-0400, 20120710160933, -0400]
            //println m[0]
            
            timezone = "UTC"
            if (m[0].size() == 3) timezone = "GMT" + m[0][2] // UTC no funka aca
            
            currentFormat = formats[ regpatterns.indexOf(pattern) ] // Formato de fecha correspondiente a la pattern
            
            d = parse(date, currentFormat) // si falla va al catch
            //println "Match $currentFormat $timezone"
            
            
            SimpleDateFormat formatter = new SimpleDateFormat(toFormat)
            formatter.setTimeZone(TimeZone.getTimeZone(timezone))
            return formatter.format(d)
         }
      }
      
      /*
      for(String pattern in patterns)
      {
         if (date.matches(pattern)) // TODO: sacarle el timezone si lo tiene
         {
            currentFormat = formats[ patterns.indexOf(pattern) ] // Formato de fecha correspondiente a la pattern
            
            d = parse(date, currentFormat) // si falla va al catch
            println "Match $currentFormat"
            println "Date $d"
            
            // FIXME: me tira siempre en el timezone local -0300
            // - Si la fecha trae TZ, usar ese
            // - Si la fecha no trae TZ, usar UTC
            //return dateFormat(d, toFormat) // ejecuta solo si el parse es correcto
            SimpleDateFormat formatter = new SimpleDateFormat(toFormat)
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
            return formatter.format(d)
         }
      }
      */
      throw new Exception("Formato desconocido: no se pudo procesar la fecha "+ date)
   }
}