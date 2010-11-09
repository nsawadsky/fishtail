package ca.ubc.spl.fishtail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TokenUtil
{
  public static List<String> getTokens(String id)
  {
    List tokens = new ArrayList();
    int start = 0;
    for (int i = 1; i < id.length(); i++) {
      if (Character.isUpperCase(id.charAt(i))) {
        tokens.add(id.substring(start, i));
        start = i;
      }
    }
    if (id.length() > start)
      tokens.add(id.substring(start, id.length()));
    return tokens;
  }

  public static String toDelimited(Collection<String> tokens, char c) {
    return toDelimited(tokens, String.valueOf(c));
  }

  public static String toDelimited(Collection<String> args, String delim) {
    if (args == null)
      return "";
    return toDelimited((String[])args.toArray(new String[0]), delim);
  }

  public static String toDelimited(String[] args, String delim) {
    String result = "";
    String[] arrayOfString = args; 
    int j = args.length; 
    for (int i = 0; i < j; i++) { 
      String s = arrayOfString[i];
      if (i != 0)
        result = result + delim;
      i++;

      result = result + s;
    }
    return result;
  }

  public static List<String> toList(String arg, char delim) {
    List args = new ArrayList();
    if (arg.length() == 0) {
      return args;
    }
    int start = 0;
    int end = 0;
    while ((end = arg.indexOf(delim, end + 1)) > 0) {
      args.add(arg.substring(start, end));

      start = end + 1;
    }
    args.add(arg.substring(start, arg.length()));
    return args;
  }

  public static boolean hasDifference(List<String> oldTerms, List<String> newTerms) {
    if ((oldTerms == null) && (newTerms == null))
      return false;
    if ((oldTerms == null) || (newTerms == null))
      return true;
    if (oldTerms.size() != newTerms.size())
      return true;
    for (String s : oldTerms)
      if (!newTerms.contains(s))
        return true;
    return false;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.TokenUtil
 * JD-Core Version:    0.6.0
 */