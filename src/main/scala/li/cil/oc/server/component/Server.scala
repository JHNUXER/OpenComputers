package li.cil.oc.server.component

import java.lang.Iterable
import java.util

import li.cil.oc.OpenComputers
import li.cil.oc.api
import li.cil.oc.api.Machine
import li.cil.oc.api.component.RackBusConnectable
import li.cil.oc.api.internal
import li.cil.oc.api.machine.MachineHost
import li.cil.oc.api.network.Analyzable
import li.cil.oc.api.network.Environment
import li.cil.oc.api.network.Message
import li.cil.oc.api.network.Node
import li.cil.oc.common.inventory.ComponentInventory
import li.cil.oc.common.inventory.ServerInventory
import li.cil.oc.common.item.Delegator
import li.cil.oc.common.GuiType
import li.cil.oc.common.InventorySlots
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item
import li.cil.oc.common.tileentity
import li.cil.oc.server.network.Connector
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.convert.WrapAsJava._

class Server(val rack: tileentity.Rack, val slot: Int) extends Environment with MachineHost with ServerInventory with ComponentInventory with Analyzable with internal.Server {
  lazy val machine = Machine.create(this)

  val node = if (rack.isServer) machine.node else null

  var wasRunning = false
  var hadErrored = false
  var lastAccess = 0L

  // ----------------------------------------------------------------------- //
  // Environment

  override def onConnect(node: Node) {
    if (node == this.node) {
      connectComponents()
    }
  }

  override def onDisconnect(node: Node) {
    if (node == this.node) {
      disconnectComponents()
    }
  }

  override def onMessage(message: Message) {
  }

  override def load(nbt: NBTTagCompound) {
    super.load(nbt)
    if (rack.isServer) {
      machine.load(nbt.getCompoundTag("machine"))
    }
  }

  override def save(nbt: NBTTagCompound) {
    super.save(nbt)
    if (rack.isServer) {
      nbt.setNewCompoundTag("machine", machine.save)
    }
  }

  // ----------------------------------------------------------------------- //
  // MachineHost

  override def internalComponents(): Iterable[ItemStack] = (0 until getSizeInventory).collect {
    case i if getStackInSlot(i) != null && isComponentSlot(i, getStackInSlot(i)) => getStackInSlot(i)
  }

  override def componentSlot(address: String) = components.indexWhere(_.exists(env => env.node != null && env.node.address == address))

  override def onMachineConnect(node: Node) = onConnect(node)

  override def onMachineDisconnect(node: Node) = onDisconnect(node)

  // ----------------------------------------------------------------------- //
  // EnvironmentHost

  override def xPosition = rack.xPosition

  override def yPosition = rack.yPosition

  override def zPosition = rack.zPosition

  override def world = rack.world

  override def markChanged() = rack.markChanged()

  // ----------------------------------------------------------------------- //
  // ServerInventory

  override def tier = Delegator.subItem(container) match {
    case Some(server: item.Server) => server.tier
    case _ => 0
  }

  override def isUseableByPlayer(player: EntityPlayer): Boolean = rack.isUseableByPlayer(player)

  // ----------------------------------------------------------------------- //
  // ItemStackInventory

  override def host = rack

  // ----------------------------------------------------------------------- //
  // ComponentInventory

  override def container = rack.getStackInSlot(slot)

  override protected def connectItemNode(node: Node) {
    if (node != null) {
      api.Network.joinNewNetwork(machine.node)
      machine.node.connect(node)
    }
  }

  override protected def onItemRemoved(slot: Int, stack: ItemStack): Unit = {
    super.onItemRemoved(slot, stack)
    if (rack.isServer) {
      val slotType = InventorySlots.server(tier)(slot).slot
      if (slotType == Slot.CPU) {
        machine.stop()
      }
    }
  }

  // ----------------------------------------------------------------------- //
  // RackMountable

  override def getData: NBTTagCompound = {
    val nbt = new NBTTagCompound()
    nbt.setBoolean("isRunning", wasRunning)
    nbt.setBoolean("hasErrored", hadErrored)
    nbt.setLong("lastAccess", lastAccess)
    nbt
  }

  override def getConnectableCount: Int = components.count {
    case Some(_: RackBusConnectable) => true
    case _ => false
  }

  override def getConnectableAt(index: Int): RackBusConnectable = components.collect {
    case Some(busConnectable: RackBusConnectable) => busConnectable
  }.apply(index)

  override def onActivate(player: EntityPlayer, side: ForgeDirection, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!player.getEntityWorld.isRemote) {
      if (player.isSneaking) {
        if (!machine.isRunning && isUseableByPlayer(player)) {
          wasRunning = false
          hadErrored = false
          machine.start()
        }
      }
      else {
        player.openGui(OpenComputers, GuiType.ServerInRack.id, world, rack.x, GuiType.embedSlot(rack.y, slot), rack.z)
      }
    }
    true
  }

  // ----------------------------------------------------------------------- //
  // ManagedEnvironment

  override def canUpdate: Boolean = true

  override def update(): Unit = {
    if (rack.isServer) {
      machine.update()

      val isRunning = machine.isRunning
      val hasErrored = machine.lastError != null
      if (isRunning != wasRunning || hasErrored != hadErrored) {
        rack.markChanged(slot)
      }
      wasRunning = isRunning
      hadErrored = hasErrored
      if (tier == Tier.Four) node.asInstanceOf[Connector].changeBuffer(Double.PositiveInfinity)
    }

    updateComponents()
  }

  // ----------------------------------------------------------------------- //
  // StateAware

  override def getCurrentState: util.EnumSet[api.util.StateAware.State] = {
    if (machine.isRunning) util.EnumSet.of(api.util.StateAware.State.IsWorking)
    else util.EnumSet.noneOf(classOf[api.util.StateAware.State])
  }

  // ----------------------------------------------------------------------- //
  // Analyzable

  override def onAnalyze(player: EntityPlayer, side: Int, hitX: Float, hitY: Float, hitZ: Float) = Array(machine.node)
}
