package it.grep.openhab.comelit.serialbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemsStateTest {

    private ItemsState itemsState;

    @BeforeEach
    void setUp() {
        itemsState = new ItemsState();
    }

    @Test
    void testSetNum_ValidRange() {
        itemsState.setNum(5);
        assertEquals(5, itemsState.getNum());
    }

    @Test
    void testSetNum_InvalidNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> itemsState.setNum(-1)
        );
        assertEquals("Invalid num value: -1", exception.getMessage());
    }

    @Test
    void testSetNum_InvalidTooLarge() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> itemsState.setNum(1001)
        );
        assertEquals("Invalid num value: 1001", exception.getMessage());
    }

    @Test
    void testSetDesc_ValidArray() {
        String[] desc = {"Light1", "Light2"};
        itemsState.setDesc(desc);
        assertArrayEquals(desc, itemsState.getDesc());
    }

    @Test
    void testSetDesc_TooLarge() {
        String[] largeArray = new String[1001];
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> itemsState.setDesc(largeArray)
        );
        assertEquals("Desc array too large: 1001", exception.getMessage());
    }

    @Test
    void testSetStatus_ValidArray() {
        int[] status = {0, 1, 2};
        itemsState.setStatus(status);
        assertArrayEquals(status, itemsState.getStatus());
    }

    @Test
    void testSetStatus_TooLarge() {
        int[] largeArray = new int[1001];
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> itemsState.setStatus(largeArray)
        );
        assertEquals("Status array too large: 1001", exception.getMessage());
    }

    @Test
    void testGetStateById_ValidIndex() {
        itemsState.setNum(3);
        itemsState.setStatus(new int[]{0, 1, 2});
        
        assertEquals(Integer.valueOf(0), itemsState.getStateById(0));
        assertEquals(Integer.valueOf(1), itemsState.getStateById(1));
        assertEquals(Integer.valueOf(2), itemsState.getStateById(2));
    }

    @Test
    void testGetStateById_InvalidIndex() {
        itemsState.setNum(2);
        itemsState.setStatus(new int[]{0, 1});
        
        assertNull(itemsState.getStateById(-1));
        assertNull(itemsState.getStateById(2));
        assertNull(itemsState.getStateById(100));
    }

    @Test
    void testGetStateById_NullStatus() {
        itemsState.setNum(2);
        itemsState.setStatus(null);
        
        assertNull(itemsState.getStateById(0));
    }

    @Test
    void testGetStateByDesc_ValidDesc() {
        itemsState.setNum(2);
        itemsState.setDesc(new String[]{"Light1", "Light2"});
        itemsState.setStatus(new int[]{0, 1});
        
        assertEquals(Integer.valueOf(0), itemsState.getStateByDesc("Light1"));
        assertEquals(Integer.valueOf(1), itemsState.getStateByDesc("Light2"));
    }

    @Test
    void testGetStateByDesc_InvalidDesc() {
        itemsState.setNum(2);
        itemsState.setDesc(new String[]{"Light1", "Light2"});
        itemsState.setStatus(new int[]{0, 1});
        
        assertNull(itemsState.getStateByDesc("NonExistent"));
        assertNull(itemsState.getStateByDesc(null));
        assertNull(itemsState.getStateByDesc(""));
    }

    @Test
    void testGetStateByDesc_NullDescArray() {
        itemsState.setNum(2);
        itemsState.setDesc(null);
        itemsState.setStatus(new int[]{0, 1});
        
        assertNull(itemsState.getStateByDesc("Light1"));
    }

    @Test
    void testGetStateByDesc_EmptyDescArray() {
        itemsState.setNum(0);
        itemsState.setDesc(new String[]{});
        itemsState.setStatus(new int[]{});
        
        assertNull(itemsState.getStateByDesc("Light1"));
    }
}