package uk.clavier.chiplate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CPUTest {
    private CPU runTest(byte[] code) {
        return this.runTest(code, false);
    }

    private CPU runTest(byte[] code, boolean falseShift) {
        Memory ram = new Memory();
        ram.loadProgram(code);

        CPU cpu = new CPU(ram, falseShift);

        for (int i = 0; i < Math.floor(code.length / 2); ++i) {
            cpu.cycle();
        }

        return cpu;
    }

    // TODO: CLS

    @Test
    public void test_jmp() {
        CPU cpu = this.runTest(new byte[] {0x12, 0x05});
        assertEquals(0x205, cpu.dumpPC());
    }

    @Test
    public void test_call() {
        CPU cpu = this.runTest(new byte[] {0x22, 0x06});
        assertEquals(0x206, cpu.dumpPC());
        assertEquals(0x202, cpu.dumpStack()[cpu.dumpSP()]);
    }

    @Test
    public void test_ret() {
        // CALL 0x202
        // RET
        byte[] code = {0x22, 0x02, 0x00, (byte) 0xEE};
        CPU cpu = this.runTest(code);

        assertEquals(0x202, cpu.dumpPC());
        assertEquals(-1, cpu.dumpSP());
    }

    @Test
    public void test_se_whenEqual() {
        // Should be init to 0x00 by default
        CPU cpu = this.runTest(new byte[] {0x30, 0x00});

        assertEquals(0x204, cpu.dumpPC());
    }

    @Test
    public void test_se_whenNotEqual() {
        CPU cpu = this.runTest(new byte[] {0x30, 0x25});

        assertEquals(0x202, cpu.dumpPC());
    }
    
    @Test
    public void test_sne_vx_byte_whenEqual() {
        // Should be init to 0x00
        CPU cpu = this.runTest(new byte[] {0x40, 0x00});

        assertEquals(0x202, cpu.dumpPC());
    }

    @Test
    public void test_sne_vx_byte_whenNotEqual() {
        CPU cpu = this.runTest(new byte[] {0x40, 0x25});

        assertEquals(0x204, cpu.dumpPC());
    }

    @Test
    public void test_mov_vx_byte() {
        CPU cpu = this.runTest(new byte[] {0x60, 0x01});
        assertEquals(0x01, cpu.dumpRegisters()[0]);
    }
    
    @Test
    public void test_add_vx_byte() {
        // mov  v0, 0x01
        // add v0, 0x09
        CPU cpu = this.runTest(new byte[] {0x60, 0x01, 0x70, 0x09});

        assertEquals(0x0A, cpu.dumpRegisters()[0]);
    }
    
    @Test
    public void test_mov_vx_vy() {
        // mov v0, 0x01
        // mov v1, v0
        byte[] code = {0x60, 0x01, (byte) 0x81, 0x00};
        CPU cpu = this.runTest(code);

        assertEquals(0x01, cpu.dumpRegisters()[1]);
    }

    @Test
    public void test_or_vx_vy() {
        byte[] code = {
            0x60, (byte) 0xAA, 
            0x61, 0x55, 
            (byte) 0x81, 0x01
        };
        CPU cpu = this.runTest(code);

        assertEquals(255, cpu.dumpRegisters()[1]);
    }

    @Test
    public void test_and_vx_vy() {
        byte[] code = {
            0x60, (byte) 0b10101011, 
            0x61, (byte) 0b01010101, 
            (byte) 0x81, 0x02
        };
        CPU cpu = this.runTest(code);

        assertEquals(1, cpu.dumpRegisters()[1]);
    }

    @Test
    public void test_xor_vx_vy() {
        byte[] code = {
            0x60, (byte) 0b11111111, 
            0x61, (byte) 0b01010101, 
            (byte) 0x81, 0x03
        };
        CPU cpu = this.runTest(code);

        assertEquals(170, cpu.dumpRegisters()[1]);
    }

    // TODO: add

    // TODO: sub

    // TODO: shr

    // TODO: subn

    // TODO: shl

    
    @Test
    public void test_sne_vx_vy_whenEqual() {
        // Should be init to 0x00
        byte[] code = {(byte) 0x90, 0x00};
        CPU cpu = this.runTest(code);

        assertEquals(0x202, cpu.dumpPC());
    }

    @Test
    public void test_sne_vx_vy_whenNotEqual() {
        byte[] code = {
            0x61, 0x01, 
            (byte) 0x91, 0x00
        };
        CPU cpu = this.runTest(code);

        assertEquals(0x206, cpu.dumpPC());
    }

    @Test
    public void test_mov_i_addr() {
        byte[] code = {
            (byte) 0xA0, 0x01
        };
        CPU cpu = this.runTest(code);

        assertEquals(0x001, cpu.dumpI());
    }
    
    @Test
    public void test_jp_v0_addr() {
        byte[] code = {
            0x60, 0x01,
            (byte) 0xB2, 0x01
        };
        CPU cpu = this.runTest(code);

        assertEquals(0x202, cpu.dumpPC());
    }

    // TODO: rnd

    // TODO: skp
    
    // TODO: sknp

    
}
