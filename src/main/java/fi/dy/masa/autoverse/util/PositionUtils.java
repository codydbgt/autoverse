package fi.dy.masa.autoverse.util;

import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;

public class PositionUtils
{
    public static final AxisAlignedBB ZERO_BB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    public static final EnumFacing[] SIDES_X = new EnumFacing[] { EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.NORTH, EnumFacing.SOUTH };
    public static final EnumFacing[] SIDES_Z = new EnumFacing[] { EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.WEST,  EnumFacing.EAST };
    public static final EnumFacing[] SIDES_Y = new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST,  EnumFacing.EAST };

    public static final int[] FACING_OPPOSITE_INDICES = new int[] { 1, 0, 3, 2, 5, 4 };

    public static final int[] FACING_INDICES_ROT90    = new int[] { 0, 1, 5, 4, 2, 3 };
    public static final int[] FACING_INDICES_ROT180   = new int[] { 0, 1, 3, 2, 5, 4 };
    public static final int[] FACING_INDICES_ROT270   = new int[] { 0, 1, 4, 5, 3, 2 };

    public static final EnumFacing[][] FROM_TO_CW_ROTATION_AXES = new EnumFacing[][] {
        { null, null, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH }, // from down
        { null, null, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH }, // from up
        { EnumFacing.EAST, EnumFacing.WEST, null, null, EnumFacing.DOWN, EnumFacing.UP }, // from north
        { EnumFacing.WEST, EnumFacing.EAST, null, null, EnumFacing.UP, EnumFacing.DOWN }, // from south
        { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.DOWN, null, null }, // from west
        { EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.UP, null, null } // from east
    };

    /** Maps the relative facing to the absolute resulting facing, for each value of the main facing.
     *  The first index is the current main facing. The second index is the current relative facing.
     *  The default value for the main facing is north, ie. that's what the relative facing is relative to.
     *  Note that for vertical facings, these use the same rotations down = +90,
     *  up = -90 around the x-axis, that the blockstate jsons use for models.
     */
    public static final EnumFacing[][] ABSOLUTE_FACING_FROM_RELATIVE_FACING_NORTH = new EnumFacing[][] {
        { EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.WEST,  EnumFacing.EAST  }, // main is down
        { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP,    EnumFacing.DOWN,  EnumFacing.WEST,  EnumFacing.EAST  }, // main is up
        { EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST,  EnumFacing.EAST  }, // main is north
        { EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST,  EnumFacing.WEST  }, // main is south
        { EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.WEST,  EnumFacing.EAST,  EnumFacing.SOUTH, EnumFacing.NORTH }, // main is west
        { EnumFacing.DOWN,  EnumFacing.UP,    EnumFacing.EAST,  EnumFacing.WEST,  EnumFacing.NORTH, EnumFacing.SOUTH }  // main is east
    };

    public static EnumFacing[] getSidesForAxis(EnumFacing axis)
    {
        switch (axis)
        {
            case UP:
            case DOWN:
                return SIDES_Y;

            case NORTH:
            case SOUTH:
                return SIDES_Z;

            default:
                return SIDES_X;
        }
    }

    @Nullable
    public static EnumFacing getCWRotationAxis(EnumFacing from, EnumFacing to)
    {
        return FROM_TO_CW_ROTATION_AXES[from.getIndex()][to.getIndex()];
    }

    public static EnumFacing rotateAround(EnumFacing facing, EnumFacing rotationAxis)
    {
        EnumFacing newFacing = facing.rotateAround(rotationAxis.getAxis());

        if (rotationAxis.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE)
        {
            return newFacing;
        }

        // Negative axis direction, if the facing was actually rotated then get the opposite
        return newFacing != facing ? newFacing.getOpposite() : facing;
    }

    /**
     * Get the rotation that will go from facingOriginal to facingRotated, if possible
     */
    public static Rotation getRotation(EnumFacing facingOriginal, EnumFacing facingRotated)
    {
        if (facingOriginal.getAxis() == EnumFacing.Axis.Y ||
            facingRotated.getAxis() == EnumFacing.Axis.Y || facingOriginal == facingRotated)
        {
            return Rotation.NONE;
        }

        if (facingRotated == facingOriginal.getOpposite())
        {
            return Rotation.CLOCKWISE_180;
        }

        return facingRotated == facingOriginal.rotateY() ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
    }

    /**
     * Rotates/transforms all the indices in the given <b>mask</b> (which represents enabled facings),
     * to match the new Rotation <b>rotation</b>.
     * @param oldMask
     * @param rotation
     * @return
     */
    public static int rotateFacingMask(int oldMask, Rotation rotation)
    {
        if (rotation != Rotation.NONE)
        {
            int[] transformedIndices = getFacingIndexRotations(rotation);
            int newMask = 0;

            for (int i = 0; i < 6; i++)
            {
                if ((oldMask & (1 << i)) != 0)
                {
                    newMask |= (1 << transformedIndices[i]);
                }
            }

            return newMask;
        }

        return oldMask;
    }

    @Nullable
    public static int[] getFacingIndexRotations(Rotation rotation)
    {
        switch (rotation)
        {
            case CLOCKWISE_90:
                return FACING_INDICES_ROT90;

            case CLOCKWISE_180:
                return FACING_INDICES_ROT180;

            case COUNTERCLOCKWISE_90:
                return FACING_INDICES_ROT270;

            default:
                return null;
        }
    }

    /**
     * This returns the given <b>facing</b> as what it would be if the <b>mainFacing</b>
     * side was NORTH, which is the default rotation for the model.
     * That way the <b>facing</b> side's texture will be placed on the correct face
     * of the non-rotated model, before the <b>mainFacing</b> rotation is applied to the entire model.
     */
    public static EnumFacing getRelativeFacing(EnumFacing mainFacing, EnumFacing facing)
    {
        switch (mainFacing)
        {
            // North is the default model rotation, don't modify the given facing for this mainFacing
            case NORTH:
                return facing;

            case SOUTH:
                if (facing.getAxis().isHorizontal())
                {
                    return facing.getOpposite();
                }
                return facing;

            default:
                EnumFacing axis = PositionUtils.getCWRotationAxis(EnumFacing.NORTH, mainFacing).getOpposite();

                if (facing.getAxis() != axis.getAxis())
                {
                    EnumFacing result = PositionUtils.rotateAround(facing, axis);
                    //System.out.printf("facing: %s axis: %s filter: %s result: %s\n", facing, axis, facingFilteredOut, result);
                    return result;
                }

                return facing;
        }
    }

    /**
     * This returns the given <b>relativeFacing</b> as an absolute facing, when the <b>mainFacing</b>
     * has been rotated from the default value of NORTH (which is the default rotation for the model) to the given value.
     */
    public static EnumFacing getAbsoluteFacingFromNorth(EnumFacing mainFacing, EnumFacing relativeFacing)
    {
        return ABSOLUTE_FACING_FROM_RELATIVE_FACING_NORTH[mainFacing.getIndex()][relativeFacing.getIndex()];
    }
}
