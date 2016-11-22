import java.util.HashMap;

public class Message {

  private static HashMap<String, Object> decodeMessage(String msg) {
    //  Decode the packed binary SIGFOX message
    //  e.g. 920e5a00b051680194597b00 -> {ctr:9.0, tmp:36.0, vlt:12.3}
    //  2 bytes name, 2 bytes float * 10, 2 bytes name, 2 bytes float * 10, ...
    HashMap<String, Object> result = new HashMap<>();
    String output = "{";
    for (int i = 0; i < msg.length(); i = i + 8) {
      String name = msg.substring(i, i + 4);
      String val = msg.substring(i + 4, i + 8);
      long name2 =
        (hexDigitToDecimal(name.charAt(2)) << 12) +
        (hexDigitToDecimal(name.charAt(3)) << 8) +
        (hexDigitToDecimal(name.charAt(0)) << 4) +
        hexDigitToDecimal(name.charAt(1));
      long val2 =
        (hexDigitToDecimal(val.charAt(2)) << 12) +
        (hexDigitToDecimal(val.charAt(3)) << 8) +
        (hexDigitToDecimal(val.charAt(0)) << 4) +
        hexDigitToDecimal(val.charAt(1));
      if (i > 0) output = output.concat(",");
      output = output.concat("\"");
      //  Decode name.
      char[] name3 = {' ', ' ', ' '};
      for (int j = 0; j < 3; j++) {
        byte code = (byte) (name2 & 31);
        char ch = decodeLetter(code);

        if (ch > 0) name3[2 - j] = ch;
        name2 = name2 >> 5;
      }
      //  Decode value.
      String name4 = String.valueOf(name3);
      float val3 = (float) (val2 / 10.0);
      result.put(name4, val3);

      output = output.concat(name4);
      output = output.concat("\":");
      output = output.concat(String.valueOf(val3));
    }
    output = output.concat("}");
    System.out.println(output);
    return result;
  }

  private final static byte firstLetter = 1;
  private final static byte firstDigit = 27;

  private static char decodeLetter(byte code) {
    //  Convert the 5-bit code to a letter.
    if (code == 0) return 0;
    if (code >= firstLetter && code < firstDigit) return (char) (code - firstLetter + 'a');
    if (code >= firstDigit) return (char) (code - firstDigit + '0');
    return 0;
  }

  private static byte hexDigitToDecimal(char ch) {
    //  Convert 0..9, a..f, A..F to decimal.
    if (ch >= '0' && ch <= '9') return (byte) (ch - '0');
    if (ch >= 'a' && ch <= 'z') return (byte) (ch - 'a' + 10);
    if (ch >= 'A' && ch <= 'Z') return (byte) (ch - 'A' + 10);
    return 0;
  }

  public static void main(String[] args) {  //  For testing.
    String msg = "920e5a00b051680194597b00";
    HashMap<String, Object> result = decodeMessage(msg);
    System.out.println(result);
  }

}
