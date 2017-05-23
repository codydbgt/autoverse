package fi.dy.masa.autoverse.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.autoverse.tileentity.TileEntityAutoverse;
import fi.dy.masa.autoverse.tileentity.TileEntityAutoverseInventory;

public class BlockAutoverseTileEntity extends BlockAutoverse
{
    public BlockAutoverseTileEntity(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        this.onBlockPlacedBy(worldIn, pos, EnumFacing.UP, state, placer, stack);
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, EnumFacing side, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null || (te instanceof TileEntityAutoverse) == false)
        {
            return;
        }

        TileEntityAutoverse teav = (TileEntityAutoverse)te;

        if (teav instanceof TileEntityAutoverseInventory && stack.hasDisplayName())
        {
            ((TileEntityAutoverseInventory)teav).setInventoryName(stack.getDisplayName());
        }

        EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, placer);

        /*if (placer.isSneaking())
        {
            facing = facing.getOpposite();
        }*/

        teav.setFacing(facing);
    }

    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityAutoverse)
            {
                ((TileEntityAutoverse)te).onLeftClickBlock(playerIn);
            }
        }
    }

    public boolean isTileEntityValid(TileEntity te)
    {
        return te != null && te.isInvalid() == false;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (worldIn.isRemote == true)
        {
            return;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityAutoverse)
        {
            ((TileEntityAutoverse) te).onNeighborBlockChange(worldIn, pos, state, blockIn);
        }
    }
}
