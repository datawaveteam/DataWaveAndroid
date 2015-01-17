package dwai.datawave;

/**
 * Created by Stefan on 1/17/2015.
 */
public class WaveUtil {
    public static byte getBit(int position, byte fromByte)
    {
        return (byte) ((fromByte >> position) & 1);
    }
}
