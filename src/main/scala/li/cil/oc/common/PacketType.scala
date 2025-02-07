package li.cil.oc.common

object PacketType extends Enumeration {
  val
  // Server -> Client
  AbstractBusState,
  Analyze,
  ChargerState,
  ColorChange,
  ComputerState,
  ComputerUserList,
  ContainerUpdate,
  DisassemblerActiveChange,
  FileSystemActivity,
  FloppyChange,
  HologramArea,
  HologramClear,
  HologramColor,
  HologramPowerChange,
  HologramRotation,
  HologramRotationSpeed,
  HologramScale,
  HologramTranslation,
  HologramValues,
  LootDisk,
  NanomachinesConfiguration,
  NanomachinesInputs,
  NanomachinesPower,
  NetSplitterState,
  ParticleEffect,
  PetVisibility, // Goes both ways.
  PowerState,
  PrinterState,
  RackInventory,
  RackMountableData,
  RaidStateChange,
  RedstoneState,
  RobotAnimateSwing,
  RobotAnimateTurn,
  RobotAssemblingState,
  RobotInventoryChange,
  RobotLightChange,
  RobotMove,
  RobotSelectedSlotChange,
  RotatableState,
  SwitchActivity,
  TextBufferInit, // Goes both ways.
  TextBufferMulti,
  TextBufferMultiColorChange,
  TextBufferMultiCopy,
  TextBufferMultiDepthChange,
  TextBufferMultiFill,
  TextBufferMultiPaletteChange,
  TextBufferMultiResolutionChange,
  TextBufferMultiMaxResolutionChange,
  TextBufferMultiSet,
  TextBufferMultiRawSetText,
  TextBufferMultiRawSetBackground,
  TextBufferMultiRawSetForeground,
  TextBufferPowerChange,
  ScreenTouchMode,
  Sound,
  SoundPattern,
  TransposerActivity,
  WaypointLabel, // Goes both ways.

  // Client -> Server
  ComputerPower,
  CopyToAnalyzer,
  DriveMode,
  DronePower,
  KeyDown,
  KeyUp,
  Clipboard,
  MouseClickOrDrag,
  MouseScroll,
  MouseUp,
  MultiPartPlace,
  RackMountableMapping,
  RackRelayState,
  RobotAssemblerStart,
  RobotStateRequest,
  ServerPower,

  EndOfList = Value
}