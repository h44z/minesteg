package at.ac.uibk.chaas.minesteg.steg;

import at.ac.uibk.chaas.minesteg.HelperUtil;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlaygroundTests {
    @Test
    public void testSendUpdate() {
        float clientPositionOld = 125.32412f;
        float clientPositionNew1 = 125.32412f;
        float clientPositionNew2 = 124.92412f;

        int encodedPosOld = MathHelper.floor(clientPositionOld * 256.0F / 360.0F);
        int encodedPosNew1 = MathHelper.floor(clientPositionNew1 * 256.0F / 360.0F);
        int encodedPosNew2 = MathHelper.floor(clientPositionNew2 * 256.0F / 360.0F);

        assertFalse(Math.abs(encodedPosNew1 - encodedPosOld) >= 1);
        assertTrue(Math.abs(encodedPosNew2 - encodedPosOld) >= 1);
    }


    @Test
    public void yawCalcTest() {
        float yawNeg = -1123.4553f;
        float yawPos = -4123.4553f;

        int negMultiplier = (int) (yawNeg / 360.0);
        int posMultiplier = (int) (yawPos / 360.0);
        float negRemainder = yawNeg % 360.0f;
        float posRemainder = yawPos % 360.0f;

        float yawNegSimple = negMultiplier * 360.0f + negRemainder;
        float yawPosSimple = posMultiplier * 360.0f + posRemainder;

        assertEquals(yawNeg, yawNegSimple);
        assertEquals(yawPos, yawPosSimple);

        byte yawNegByte = HelperUtil.getWireValue(yawNeg);
        byte yawPosByte = HelperUtil.getWireValue(yawPos);
        byte yawNegRemainderByte = HelperUtil.getWireValue(negRemainder);
        byte yawPosRemainderByte = HelperUtil.getWireValue(posRemainder);

        assertEquals(yawNegByte, yawNegRemainderByte);
        assertEquals(yawPosByte, yawPosRemainderByte);
    }

    @Test
    public void minMaxTest() {
        float value = 123.4553f;
        byte byteValue = HelperUtil.getWireValue(value);

        float min = ((byteValue) * 360.0F / 256.0F) + 0.001f;
        float max = ((byteValue + 1) * 360.0F / 256.0F) - 0.001f;

        byte byteMin = HelperUtil.getWireValue(min);
        byte byteMax = HelperUtil.getWireValue(max);

        assertEquals(byteValue, byteMin);
        assertEquals(byteValue, byteMax);
    }

    @Test
    public void yawMatchTest() {
        float yawNeg = -1123.4553f; // -31
        float yawPos = 4123.4553f; // 116

        byte byteValueNeg = -22;
        byte byteValuePos = 119;

        int multiplierNeg = (int) (yawNeg / 360.0F);
        int multiplierPos = (int) (yawPos / 360.0F);

        float yawNegNew = (byteValueNeg * 360.0F / 256.0F) + multiplierNeg * 360.0F;
        float yawPosNew = (byteValuePos * 360.0F / 256.0F) + multiplierPos * 360.0F;

        assertTrue(Math.abs(yawNeg - yawNegNew) < 40);
        assertTrue(Math.abs(yawPos - yawPosNew) < 40);
    }

    @Test
    public void yawMatchTest2() {
        float yawNeg = -360.4553f; // -1
        float yawPos = 721.4553f; // 1

        byte byteValueNegInfo = HelperUtil.getWireValue(yawNeg);
        byte byteValuePosInfo = HelperUtil.getWireValue(yawPos);

        byte byteValueNeg = 1;
        byte byteValuePos = -2;

        int multiplierNeg = (int) (yawNeg / 360.0F);
        int multiplierPos = (int) (yawPos / 360.0F);

        float yawNegNew = (byteValueNeg * 360.0F / 256.0F) + multiplierNeg * 360.0F;
        float yawPosNew = (byteValuePos * 360.0F / 256.0F) + multiplierPos * 360.0F;

        assertTrue(Math.abs(yawNeg - yawNegNew) < 40);
        assertTrue(Math.abs(yawPos - yawPosNew) < 40);
    }

    @Test
    public void randomizeYawTest() {
        Encoder enc = new Encoder(null, null, null);
        float yaw = -197.17036f;
        float yaw2 = -229.93044f;
        float yawNeg = -360.4553f;
        float yawPos = 721.4553f;
        float yawPosCloseEdge = 360.4553f;
        float yawNegCloseEdge = -719.5553f;
        float yawPosCloseEdge2 = 359.5553f;
        float yawNegCloseEdge2 = -720.4553f;


        float yawNew = enc.randomizeYaw(yaw);
        float yawNew2 = enc.randomizeYaw(yaw2);
        float yawNegNew = enc.randomizeYaw(yawNeg);
        float yawPosNew = enc.randomizeYaw(yawPos);
        float yawPosCloseEdgeNew = enc.randomizeYaw(yawPosCloseEdge);
        float yawNegCloseEdgeNew = enc.randomizeYaw(yawNegCloseEdge);
        float yawPosCloseEdgeNew2 = enc.randomizeYaw(yawPosCloseEdge2);
        float yawNegCloseEdgeNew2 = enc.randomizeYaw(yawNegCloseEdge2);

        assertTrue(Math.abs(yaw - yawNew) < 2);
        assertTrue(Math.abs(yaw2 - yawNew2) < 2);
        assertTrue(Math.abs(yawNeg - yawNegNew) < 2);
        assertTrue(Math.abs(yawPos - yawPosNew) < 2);
        assertTrue(Math.abs(yawPosCloseEdge - yawPosCloseEdgeNew) < 2);
        assertTrue(Math.abs(yawNegCloseEdge - yawNegCloseEdgeNew) < 2);
        assertTrue(Math.abs(yawPosCloseEdge2 - yawPosCloseEdgeNew2) < 2);
        assertTrue(Math.abs(yawNegCloseEdge2 - yawNegCloseEdgeNew2) < 2);

        assertEquals(HelperUtil.getWireValue(yaw), HelperUtil.getWireValue(yawNew));
        assertEquals(HelperUtil.getWireValue(yaw2), HelperUtil.getWireValue(yawNew2));
        assertEquals(HelperUtil.getWireValue(yawNeg), HelperUtil.getWireValue(yawNegNew));
        assertEquals(HelperUtil.getWireValue(yawPos), HelperUtil.getWireValue(yawPosNew));
        assertEquals(HelperUtil.getWireValue(yawPosCloseEdge), HelperUtil.getWireValue(yawPosCloseEdgeNew));
        assertEquals(HelperUtil.getWireValue(yawNegCloseEdge), HelperUtil.getWireValue(yawNegCloseEdgeNew));
        assertEquals(HelperUtil.getWireValue(yawPosCloseEdge2), HelperUtil.getWireValue(yawPosCloseEdgeNew2));
        assertEquals(HelperUtil.getWireValue(yawNegCloseEdge2), HelperUtil.getWireValue(yawNegCloseEdgeNew2));
    }

    @Test
    public void randomizePitchTest() {
        Encoder enc = new Encoder(null, null, null);
        float pitch = -0.4553f;
        float pitchNeg = -56.4553f;
        float pitchPos = 25.4553f;
        float pitchPosCloseEdge = 89.4553f;
        float pitchNegCloseEdge = -89.5553f;
        float pitchPosCloseEdge2 = 0.5553f;
        float pitchNegCloseEdge2 = -0.4553f;

        float pitchNew = enc.randomizePitch(pitch);
        float pitchNegNew = enc.randomizePitch(pitchNeg);
        float pitchPosNew = enc.randomizePitch(pitchPos);
        float pitchPosCloseEdgeNew = enc.randomizePitch(pitchPosCloseEdge);
        float pitchNegCloseEdgeNew = enc.randomizePitch(pitchNegCloseEdge);
        float pitchPosCloseEdgeNew2 = enc.randomizePitch(pitchPosCloseEdge2);
        float pitchNegCloseEdgeNew2 = enc.randomizePitch(pitchNegCloseEdge2);

        assertTrue(Math.abs(pitch - pitchNew) < 2);
        assertTrue(Math.abs(pitchNeg - pitchNegNew) < 2);
        assertTrue(Math.abs(pitchPos - pitchPosNew) < 2);
        assertTrue(Math.abs(pitchPosCloseEdge - pitchPosCloseEdgeNew) < 2);
        assertTrue(Math.abs(pitchNegCloseEdge - pitchNegCloseEdgeNew) < 2);
        assertTrue(Math.abs(pitchPosCloseEdge2 - pitchPosCloseEdgeNew2) < 2);
        assertTrue(Math.abs(pitchNegCloseEdge2 - pitchNegCloseEdgeNew2) < 2);

        assertEquals(HelperUtil.getWireValue(pitch), HelperUtil.getWireValue(pitchNew));
        assertEquals(HelperUtil.getWireValue(pitchNeg), HelperUtil.getWireValue(pitchNegNew));
        assertEquals(HelperUtil.getWireValue(pitchPos), HelperUtil.getWireValue(pitchPosNew));
        assertEquals(HelperUtil.getWireValue(pitchPosCloseEdge), HelperUtil.getWireValue(pitchPosCloseEdgeNew));
        assertEquals(HelperUtil.getWireValue(pitchNegCloseEdge), HelperUtil.getWireValue(pitchNegCloseEdgeNew));
        assertEquals(HelperUtil.getWireValue(pitchPosCloseEdge2), HelperUtil.getWireValue(pitchPosCloseEdgeNew2));
        assertEquals(HelperUtil.getWireValue(pitchNegCloseEdge2), HelperUtil.getWireValue(pitchNegCloseEdgeNew2));
    }

    @Test
    public void byteConversionTest() {
        int angle = 350;
        byte bAngle = (byte) angle;

        int iAngle = bAngle & 0xFF;

        assertEquals(angle % 256, iAngle);
    }

    @Test
    public void pitchConversionTest() {
        Encoder enc = new Encoder(null, null, null);
        byte pitchByte = 62;

        float pitch = enc.convertWirePitch(pitchByte);

        assertTrue(pitch <= 90.0F);
        assertTrue(pitch >= -90.0F);
    }

    @Test
    public void yawConversionTest() {
        Encoder enc = new Encoder(null, null, null);
        byte yawByte = 62;
        byte yawByte1 = -63;
        byte yawByte2 = 126;
        byte yawByte3 = 0;
        float prevYaw = 266.436f;

        float yaw = enc.convertWireYaw(yawByte, prevYaw);
        float yaw1 = enc.convertWireYaw(yawByte1, prevYaw);
        float yaw2 = enc.convertWireYaw(yawByte2, prevYaw);
        float yaw3 = enc.convertWireYaw(yawByte3, prevYaw);

        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);
        assertTrue(Math.abs(prevYaw - yaw2) < 180);
        assertTrue(Math.abs(prevYaw - yaw2) < 180);
        assertTrue(Math.abs(prevYaw - yaw3) < 180);
        assertTrue(Math.abs(prevYaw - yaw3) < 180);

        assertEquals(yawByte, HelperUtil.getWireValue(yaw));
        assertEquals(yawByte1, HelperUtil.getWireValue(yaw1));
        assertEquals(yawByte2, HelperUtil.getWireValue(yaw2));
        assertEquals(yawByte3, HelperUtil.getWireValue(yaw3));
    }

    @Test
    public void yawConversionTest2() {
        Encoder enc = new Encoder(null, null, null);
        byte yawByte = 62;
        byte yawByte1 = -63;
        byte yawByte2 = 126;
        byte yawByte3 = 0;
        float prevYaw = -719.436f;

        float yaw = enc.convertWireYaw(yawByte, prevYaw);
        float yaw1 = enc.convertWireYaw(yawByte1, prevYaw);
        float yaw2 = enc.convertWireYaw(yawByte2, prevYaw);
        float yaw3 = enc.convertWireYaw(yawByte3, prevYaw);

        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);
        assertTrue(Math.abs(prevYaw - yaw2) < 180);
        assertTrue(Math.abs(prevYaw - yaw2) < 180);
        assertTrue(Math.abs(prevYaw - yaw3) < 180);
        assertTrue(Math.abs(prevYaw - yaw3) < 180);

        assertEquals(yawByte, HelperUtil.getWireValue(yaw));
        assertEquals(yawByte1, HelperUtil.getWireValue(yaw1));
        assertEquals(yawByte2, HelperUtil.getWireValue(yaw2));
        assertEquals(yawByte3, HelperUtil.getWireValue(yaw3));
    }

    @Test
    public void yawConversionTest3() {
        Encoder enc = new Encoder(null, null, null);
        byte yawByte = -65;
        byte yawByte1 = -72;
        float prevYaw = -91.2085f;

        float yaw = enc.convertWireYaw(yawByte, prevYaw);
        float yaw1 = enc.convertWireYaw(yawByte1, prevYaw);

        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);

        assertEquals(yawByte, HelperUtil.getWireValue(yaw));
        assertEquals(yawByte1, HelperUtil.getWireValue(yaw1));
    }

    @Test
    public void yawConversionTest4() {
        Encoder enc = new Encoder(null, null, null);
        byte yawByte = 35;
        byte yawByte1 = 48;
        float prevYaw = 409.66763f;

        float yaw = enc.convertWireYaw(yawByte, prevYaw);
        float yaw1 = enc.convertWireYaw(yawByte1, prevYaw);

        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);
        assertTrue(Math.abs(prevYaw - yaw1) < 180);

        assertEquals(yawByte, HelperUtil.getWireValue(yaw));
        assertEquals(yawByte1, HelperUtil.getWireValue(yaw1));
    }
}
