package tool;


import org.tartarus.snowball.SnowballProgram;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Tokenizer
 *
 *  @author  Peter Cheng
 *
 */
public   class  Tokenizer  {
    /**
     * Language
     */
    public   static  String language  =   "English" ;

    /*  Stemmer  */
    private   static  SnowballProgram stemmer  =   null ;

    /*  Stem method  */
    private   static  Method stemMethod  =   null ;

    /**
     * Tokenize and stem
     *
     *  @param  source
     *            The string to be processed
     *  @return  All the word stems
     */
    public   static  Iterator tokenize(String source)  {
        if  (Tokenizer.stemmer  ==   null )  {
            try   {
                Class stemClass  =  Class.forName( "org.tartarus.snowball.ext."
                        +  Tokenizer.language  +   "Stemmer" );
                Tokenizer.stemmer  =  (SnowballProgram) stemClass.newInstance();
                Tokenizer.stemMethod  =  stemClass
                        .getMethod( "stem" ,  new  Class[ 0 ]);
            }   catch  (Exception e)  {
                System.out.println( " Error when initializing Stemmer! " );
                System.exit( 1 );
            }
        }

         /*  Tokenizer  */
        ArrayList tokens  =   new  ArrayList();
        StringBuffer buffer  =   new  StringBuffer();
        for  ( int  i  =   0 ; i  <  source.length(); i ++ )  {
            char  character  =  source.charAt(i);
            if  (Character.isLetter(character))  {
                buffer.append(character);
            }   else   {
                if  (buffer.length()  >   0 )  {
                    tokens.add(buffer.toString());
                    buffer  =   new  StringBuffer();
                }
            }
        }
        if  (buffer.length()  >   0 )  {
            tokens.add(buffer.toString());
        }

         /*  All the words  */
        ArrayList words  =   new  ArrayList();

         /*  All the words consisting of capitals  */
        ArrayList allTheCapitalWords  =   new  ArrayList();

         /*  Tokenize according to the capitals  */
        nextToken:  for  (Iterator allTokens  =  tokens.iterator(); allTokens
                .hasNext();)  {
            String token  =  (String) allTokens.next();

             /*  The words consisting of capitals  */
            boolean  allUpperCase  =   true ;
            for  ( int  i  =   0 ; i  <  token.length(); i ++ )  {
                if  ( ! Character.isUpperCase(token.charAt(i)))  {
                    allUpperCase  =   false ;
                }
            }
            if  (allUpperCase)  {
                allTheCapitalWords.add(token);
                continue  nextToken;
            }

             /*  Other cases  */
            int  index  =   0 ;
            nextWord:  while  (index  <  token.length())  {
                nextCharacter:  while  ( true )  {
                    index ++ ;
                    if  ((index  ==  token.length())
                            ||   ! Character.isLowerCase(token.charAt(index)))  {
                        break  nextCharacter;
                    }
                }
                words.add(token.substring( 0 , index).toLowerCase());
                token  =  token.substring(index);
                index  =   0 ;
                continue  nextWord;
            }
        }

         /*  Stemming  */
        try   {
            for  ( int  i  =   0 ; i  <  words.size(); i ++ )  {
                Tokenizer.stemmer.setCurrent((String) words.get(i));
                Tokenizer.stemMethod.invoke(Tokenizer.stemmer,  new  Object[ 0 ]);
                words.set(i, Tokenizer.stemmer.getCurrent());
            }
        }   catch  (Exception e)  {
            e.printStackTrace();
        }

        words.addAll(allTheCapitalWords);

        return  words.iterator();
    }

    public static void main(String[] args) {
        String str = "baby are running";
        Iterator it = tokenize(str);
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        System.out.println(token("bottle"));
    }

    public static String token(String input) {
        StringBuffer result = new StringBuffer();
        Iterator it = tokenize(input);
        while (it.hasNext()) {
            result.append(it.next()).append(" ");
        }
        return result.toString().trim();
    }

}
