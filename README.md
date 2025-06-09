hi mrs kas: i redid it with the template thing

/******************************************************************************
 *  Name:     Johnny Li
 *
 *  Partner Name:      Derek Zhang  
 *
 ******************************************************************************/

Final Project Name: Italian Brainrot Tower Defense

/******************************************************************************
 *  Describe how decided to implement this project. Is it original? or a spin-off
 *  a similar project? If it is the latter, submit a link or documentation to it.
 *****************************************************************************/
This project is based on the game Bloons Tower Defense. We created a spin off with Italian brainrot meme theme. We chose this because it is a familiar genre(tower defense) combined with a current meme.
The original series: https://btd6.com/

We implemented it using Java's Swing for the GUI with JPanel to render the visuals and a timer for the main loop.
The classes are used like this:
MainFrame as the primary application window.
GamePanel to handle game logic, rendering, and user interactions.
GameState to encapsulate core game variables (money, lives, wave number).
Entity classes like Human (enemies), Monkey (towers), and Projectile.
UI helper panels like SideInfoPanel (for buying towers and game info) and UpgradeControlPanel (for tower upgrades).
SpriteManager for efficient image loading and caching.
This modular approach was chosen for better organization and clear separation of concerns, which was important because of the multiple components of the game.

/******************************************************************************
 *  Describe step by step how to execute your project successfully.
 * If multiple conditions result in different outputs, describe the steps 
 * to achieve the different outcomes.
 *u
 ******************************************************************************/
Assuming you have a java JDK, place all .java and .png files in the same directory. Open MainFrame.java using VSCODE. Compile and Run.
A window will appear. The game begins paused, you may click the start wave button(top right) once you are ready. Click the buy buttons below to select monkeys to place down, after clicking on a buy button, click on a spot on the map to place your monkey(within boundaries and not on path). Once a monkey is placed, you can see its range and modify the monkey's stats by selling or upgrading it by clicking on it. 

Survive waves by strategically upgrading and placing monkeys. If a human reaches the end of the path, then you will lose lives depending on how many points that human is worth(weaker=less points)

There is no game win, the waves are infinite and get progressively harder. Once the lives reach 0, the game will end, prompting for a game restart. Clicking the restart button will reset to initial state.
/******************************************************************************
 *  Describe the data types you used to implement  your project
 *  
 *****************************************************************************/

 Custom classes:
   MainFrame: The main window container (extends JFrame).
   GamePanel: Manages game rendering, game loop, user input, and entity management (extends JPanel).
   GameState: Tracks core game variables like money, lives, and wave number.
   Monkey: Base class for player-controlled towers, handling placement, targeting, shooting, and upgrades.
   MonkeyB, MonkeyC: Specialized subclasses of Monkey with unique abilities (explosive, slowing).
   Human: Represents enemy units, managing their health, movement along a path, and type.
   Map: Defines the path for Human enemies using a list of coordinates.
   Projectile: Represents projectiles fired by Monkey objects, handling their movement, damage, and special effects (e.g. explosion, slow).
   SideInfoPanel: UI panel displaying game state (money, lives) and options to buy monkeys (extends JPanel).
   UpgradeControlPanel: UI panel for upgrading and selling selected Monkey instances (extends JPanel).
   UpgradeGUI: Handles the visual drawing and interaction logic within the UpgradeControlPanel.
   SpriteManager: Manages loading, caching, and resizing of image sprites.
   WaveDefinition (Inner class in GamePanel): Defines the composition of an enemy wave using a list of SpawnInstructions.
   SpawnInstruction (Inner class in GamePanel): Defines a group of enemies (type, count, delay, interval) to spawn within a wave.

Standard Java Data Types:
   Primitive Types:
       int: Used for counts (e.g., humansKilled, currentWave), money, health, coordinates, levels, pixel dimensions, array indices, ticks.
       double: Used for precise coordinates, speed, range, rates, angles, percentages.
       boolean: Used for flags and conditions (e.g., isPlacingMonkey, gameIsOver, camo, isSelected, placementValid).
       long: Used for time-based calculations (e.g., lastShotTime, slowEffectEndTimeMillis).
   Object Types (from Java Standard Library):
       String: For identifiers (monkey types, human types, archetype names), file paths for sprites, UI text labels.
       List<E>: To store collections of Monkey objects, Human objects, Projectile objects, path coordinates (java.awt.Point), and SpawnInstructions within a WaveDefinition.
       Map<K, V> (primarily java.util.HashMap<K, V>): Used in SpriteManager to cache BufferedImages (value) by their String file path (key).
       java.awt.Point: To store (x, y) coordinates, particularly for defining the enemy path in the Map class.
       java.awt.Rectangle: For defining clickable areas for UI buttons and potentially for hitboxes.
       java.awt.Color: For UI elements, drawing projectiles, range indicators, and visual effects.
       java.awt.image.BufferedImage: To store and manipulate images/sprites loaded from files.
       java.util.Random: For any procedural generation aspects, like selecting sprite variants or defining procedural waves.
       javax.swing.JLabel, javax.swing.JButton: Core Swing components used in SideInfoPanel and other UI parts.
       java.awt.Font: For styling text in UI elements.
       java.awt.Graphics, java.awt.Graphics2D: Used in paintComponent methods for drawing.
       Enum (for Projectile.State): To represent discrete states of a projectile (active, exploding, spent).





/******************************************************************************
 *  Describe the methods used in your ADTs
 *****************************************************************************/

1.  Constructor Methods:
       Purpose: Initialize new objects, set up their initial state, load default values, and establish dependencies.
       Examples: GamePanel(), GameState(initialLives, initialMoney), Monkey(x, y, type), Human(type, map), Projectile(...).

2.  Game Logic and Update Methods:
       Purpose: Implement core game mechanics, entity behavior, and state transitions during each game tick.
       Examples:
           GamePanel.updateGame(): Main game loop logic; updates humans, monkeys, projectiles, checks wave status, handles spawning.
           Human.move(List<Point> path): Advances human along the path.
           Human.takeDamage(int amount): Reduces human's health.
           Monkey.findTarget(List<Human> humans): Selects a human to attack within range.
           Monkey.shoot(Human target): Creates and fires a projectile.
           Monkey.updateProjectiles(List<Human> humans, GameState gs): Updates projectiles fired by this monkey.
           Projectile.update(List<Human> humans, GameState gs): Moves projectile, checks for hits, applies effects.
           GameState.addMoney(int amount), GameState.loseLife(), GameState.incrementWave().

3.  Drawing and Rendering Methods:
       Purpose: Responsible for drawing all visual elements onto the screen.
       Examples:
           paintComponent(Graphics g): Overridden in GamePanel, SideInfoPanel, UpgradeControlPanel, UpgradeGUI to draw respective elements.
           Helper methods within these classes like drawHumans(Graphics2D g), drawMonkeys(Graphics2D g), drawProjectiles(Graphics2D g), drawUI(Graphics g).
           SpriteManager.getScaledSprite(...): Provides images for drawing.

4.  Event Handling Methods:
       Purpose: Respond to user input, such as mouse clicks and movements.
       Examples:
           Methods from MouseListener and MouseMotionListener (e.g., mousePressed, mouseMoved, mouseClicked) implemented in GamePanel (for placing/selecting monkeys), UpgradeGUI (for upgrade/sell buttons), and SideInfoPanel (for buy buttons if custom-drawn).
           actionPerformed(ActionEvent e) for JButtons in SideInfoPanel (e.g., "Start Game" button, monkey buy buttons).

5.  Setter Methods (Mutators):
       Purpose: Modify the internal state or properties of objects.
       Examples: Monkey.setSelected(boolean), Human.applySlow(double rate, int duration), GameState.setMoney(int amount).

6.  Getter Methods (Accessors):
       Purpose: Provide read-only access to an object's internal state or properties.
       Examples: Monkey.getRange(), Human.getHealth(), GameState.getMoney(), SpriteManager.getSprite(String path).

7.  Utility and Helper Methods:
       Purpose: Perform specific supporting tasks like calculations, resource loading, or object creation.
       Examples:
           SpriteManager.loadSprite(String path), SpriteManager.resizeImage(BufferedImage, int, int).
           Methods for calculating distance between points, angles for projectiles.
           GamePanel.spawnHuman(String type, boolean isCamo): Creates and adds a human to the game.
           GamePanel.isValidPlacement(int x, int y, double hitbox): Checks if a monkey can be placed at given coordinates.

8.  Upgrade and Archetype Methods:
       Purpose: Manage the progression and specialization of monkeys.
       Examples:
           Monkey.upgrade(): Applies stat boosts according to its current level/path.
           Monkey.canAffordUpgrade(int currentMoney)
           Monkey.setArchetype(String archetype): Commits monkey to a specific upgrade path.
           Methods in UpgradeGUI to display upgrade options, costs, and process clicks related to upgrades, archetype choices, and selling.
           Monkey.getSellValue(): Calculates money returned on selling.

9.  UI Component Styling and Creation Methods:
       Purpose: Specifically for setting up and styling Swing UI components.
       Examples:
           Methods in SideInfoPanel to create and configure JButtons for buying monkeys and JLabels for displaying info.
           Methods in UpgradeGUI to define button bounds, colors, fonts, and text for the upgrade interface.







/******************************************************************************
 *  Describe the data needed for your project.
 *  Submit data file(s) to run your project. What is the name of the data file(s)?
 *  Describe the purpose of the data.
 *  Describe the multiple testing done to demonstrate a successful implementation.
 *****************************************************************************/
This project uses sprites(images) to run. 

   map_background.png: Image file used as the background for the game map.
   human_baby.png: Sprite for the "baby" human enemy.
   human_kid0.png, human_kid1.png: Sprites for "kid" human enemy variants.
   human_normal0.png, human_normal1.png, human_normal2.png: Sprites for "normal" human enemy variants.
   human_bodybuilder.png: Sprite for the "bodybuilder" human enemy.
   human_businessman.png: Sprite for the "businessman" human enemy.
   human_bigbaby.png: Sprite for the "big baby" (boss) human enemy.
   human_ninja.png: Sprite for the "ninja" human enemy.
   human_bossninja.png: Sprite for the "boss ninja" human enemy.
   monkey_base_idle.png: Sprite for the default monkey when idle.
   monkey_base_shoot.png: Sprite for the default monkey when shooting.
   monkey_bomber_idle.png: Sprite for the Bomber Monkey when idle.
   monkey_bomber_shoot.png: Sprite for the Bomber Monkey when shooting.
   monkey_slow_idle.png: Sprite for the Slow Monkey when idle.
   monkey_slow_shoot.png: Sprite for the Slow Monkey when shooting.
   explosion_effect.png: Sprite for the visual effect of explosions (Bomber Monkey).
   project_slow.png: Sprite for the slow projectile or its impact/area effect (from Slow Monkey).
   projectile_dart.png: Default sprite for standard projectiles.
   projectile_bomb.png: Sprite for bomb projectiles.

The data is used to render colorful and detailed objects, that would be highly difficult and inefficient if using default shapes in java. The SpriteManager class is responsible for loading and managing these images.


The game has been played through(till lives are 0) ~50 times, no issues arise.


 

/******************************************************************************
 *  Known bugs/limitations.
 *****************************************************************************/
Performance when there are a significant amount of Human and Projectile objects will be degraded, as a limitation of Java Swing.
Balancing a game is difficult, some troops are inherently stronger than others.
There are no save features. Closing the program means losing progress, if mid game.
There is no audio
Projectile hitboxes are circles, not the exact pixels of the sprites.

/******************************************************************************
 *  Describe whatever help (if any) that you received.
 *  Don't include readings, lectures, and precepts, but do
 *  include any help from people (including course classmates, and friends)
 *  and attribute them by name.
 *****************************************************************************/
I asked AI language models for help on drawing the objects into a java window.

/******************************************************************************
 *  Describe any serious problems you encountered.                    
 *****************************************************************************/
i had no idea how to do rendering so i asked ai for help

/***************************************************************************************
* Collaboration with a partner.  Both students must work together and discuss, write,
* debug, test, analyze, document, and submit all elements of the assignment together.
* Both partners are responsible for understanding all parts of the submitted assignment
* and receive the same grade. If two students begin working on an assignment as partners
* and cannot complete it together, (at least) one student must contact me to request a 
* partnership dissolution.
 **************************************************************************************/

 


/******************************************************************************
 *  List any other comments here. Feel free to provide any feedback   
 *  on  how helpful the class meeting was and on how much you learned 
 * from doing the assignment, and whether you enjoyed doing it.
 *****************************************************************************/






previous readme

# **Italian Brainrot Tower Defense** 

## Contributors
Derek Zhang
Johnny Li

## Intro

This project is a simple tower defense game It is the combination of **BloonsTD** with **italian brainrot**. The main objective of the game is to survive as many rounds as possible by placing and upgrading italian brainrot characters. Enemies are different variants of humans attempting to traverse the path. If a human makes it past, then you lose a life. 

The <ins> GamePanel</ins> class handles visuals (drawing), user input, and the game loop This includes movement of enemies (humans), monkeys (characters) and wave management 

The <ins> GameState</ins> class as suggested in the name, manages the state of the game This includes the money, lives, and wave number 

The <ins> Monkey</ins> class creates the characters to be placed and manages the upgrading stats and attacking <ins> MonkeyB</ins> and <ins> MonkeyC</ins> are both child classes of Monkey with specilized abilities: (Exposives and Slowing Projectiles respectively)

<ins> Human</ins> objects are the enemies moving along the path and this movement is handled by the <ins> Map</ins> class. They are able to have different variants.

<ins> Projectile</ins> represents the objects (projectiles) fired by all monkeys 

The <ins> SideInfoPanel</ins> class handles the rightmost UI panel, showing general information such as money, wave number, and the characters

The <ins> UpgradeControlPanel</ins> class inclues a pop-up, allowing for the characters to be upgraded 

The <ins> SpriteManager</ins> handles loading the images as well as resizing them

<ins> Mainframe</ins> is the "main" class This is the class to run to start the application and sets up the main game window and panels


## Variables Used



This section explains all important variables used across all classes
‎
**GamePanel Class Variables:**

```
defaultMonkey (String): constant string identifier for the default monkey type

bomberMonkey (String): constant string identifier for the bomber monkey type

slowMonkey (String): constant string identifier for the slow monkey type

monkeys (List): list to store all active Monkey objects

humans (List): list to store all active Human objects

selectedMonkey (Monkey): refers to the current selected Monkey (used for upgrading and selling)

upgradeControlPanelRef (UpgradeControlPanel): refers to the UI panel for upgrades

gameState (GameState): refers to the GameState object managing game-wide parameters

map (Map): gamemap, defining a path for humans

random (Random): random object used for procedural generation

waveDefinitions (List): list of defined wave configurations

isPlacingMonkey (boolean): boolean indicating if the user is currently in monkey placement mode

placingMonkeyType (String): type of monkey being placed

placementPreviewMonkey (Monkey): temporary monkey shown before placement to indicate position and range

placementValid (boolean): boolean indicating if the current monkey placement position is valid

placementMouseX, placementMouseY (int): mouse coordinates during monkey placement

separationBuffer (double): buffer distance used to ensure monkeys are not placed too close to each other or the path

gameIsOver (boolean): boolean indicating if the game has ended

restartButtonBounds (Rectangle): defines the bounds of the restart game button

restartButtonHoverColor (Color): color of the restart button when hovered over

currentSpawnCooldownTicks (int): countdown for the delay between enemy spawns

currentProceduralWaveDefinition (WaveDefinition): defines a procedurally generated wave

lastProceduralWaveGenerated (int): stores the wave number for which the last procedural wave was generated

```
**GameState Class Variables:**
```
money (int):  amount of money the player has

bloonsKilled (int): total number of enemies killed by player

currentWave (int): current wave number

lives (int): number of lives left

initialLives (int): starting number of lives for the game

initialMoney (int): starting amount of money for the game
```
**Human Class Variables:**
```
x, y (double): current coordinates of the human on the map

currentSpeed (double): current movement speed of the human

originalSpeed (double): base movement speed of the human

hitboxDiameter (int): diameter of the humans collision box

health (int): current health points of the human

currentPathIndex (int): index of the current waypoint the human is moving towards

camo (boolean): boolean for if the human is camo (invisible to non-camo detecting monkeys)

reachedEnd (boolean): boolean indicating if the human has reached the end of the path

sprite (BufferedImage): image used to draw the human

currentAngleRadians (double): current rotation angle of the human sprite

humanType (String): specific type of human (ex "human_baby)

moneyReward (int): amount of money rewarded to the player when human is killed

slowEffectEndTimeMillis (long): time when any slow effect on the human will end

slowRate (double): multiplier for slowing down human speed

random (Random): random object for selecting sprites

currentSpriteVariants (List): list of possible sprite paths for this human type

defaultCamoForType (boolean): boolean for if this human type is camo by default
```

Map Class Variables:
```
pathCoordinates (ArrayList): list of Point objects defining the points of the path for the enemies to follow
```
Monkey Class Variables:
```
x, y (int): current coordinates of the monkey on the map

range (double): radius within which the monkey can detect and target humans

hitbox (double): diameter of the monkey's collision box for placement

upgradeCost (int): cost to upgrade the monkey to the next level

level (int): current level of the monkey

projectiles (List): list of projectiles currently fired by this monkey

monkeyColor (Color): base color used for drawing the monkey if no sprite is available

COST (int): static constant for the base cost of a default monkey

DEFAULT_INITIAL_RANGE (double): static constant for the initial range of a default monkey

DEFAULT_INITIAL_HITBOX (double): static constant for the initial hitbox of a default monkey

projectileColor (Color): Color of the projectiles fired by this monkey

projectileRadius (int): radius of the projectiles

projectileSpeed (double): speed of the projectiles

projectileDamage (int): damage dealt by the projectiles

projectileIsExplosive (boolean):boolean for if a projectile is explosive

projectileAoeRadius (double): (AoE) radius for explosive projectiles

projectileExplosionVisualColor (Color): color of the visual explosion effect

projectileExplosionVisualDuration (int): duration of the visual explosion effect in game ticks

projectileExplosionSpritePath (String): path to the sprite for the explosion visual

lastShotTime (long): last time the monkey fired a projectile

shootCooldown (long): reload speed, time between shots

isSelected (boolean): boolean indicating if the monkey is currently selected by the user

canSeeCamo (boolean): true if the monkey can detect camo humans

idleSprite (BufferedImage): image used when the monkey is idle

shootingSprite (BufferedImage): image used when the monkey is shooting

idleSpritePath (String): file path for the idle sprite

shootingSpritePath (String): file path for the shooting sprite

isAnimatingShot (boolean): boolean indicating if the monkey is currently playing a shooting animation

shotAnimationEndTime (long): time for when the shooting animation should end

SHOT_ANIMATION_DURATION_MS (long): duration of the shooting animation

lastShotAngleRadians (double): angle at which the last shot was fired, used for sprite rotation

archetypeNONE (String): constant for no chosen archetype

archetypeDART_SNIPER (String): constant for Dart Monkey Sniper archetype

archetypeDART_QUICKFIRE (String): constant for Dart Monkey Quickfire archetype

archetypeBOMB_FRAGS (String): constant for Bomb Monkey Frags archetype

archetypeBOMB_CONCUSSION (String): constant for Bomb Monkey Concussion archetype

archetypeICE_PERMAFROST (String): constant for Ice Monkey Permafrost archetype

archetypeICE_BRITTLE (String): constant for Ice Monkey Brittle archetype

chosenArchetype (String): identifier for the chosen upgrade path/archetype

hasChosenArchetype (boolean): boolean indicating if an archetype has been selected

totalSpentOnMonkey (int): total money spent on purchasing and upgrading this monkey

sellPercentage (double): percentage of total spent money that is returned when selling a monkey
```
MonkeyB Class Variables:
```
COST (int): static constant for the base cost of a Bomber Monkey

monkeyBInitialRange (double): static constant for the initial range of a Bomber Monkey

monkeyBInitialHitbox (double): static constant for the initial hitbox of a Bomber Monkey

monkeyBIDLEspritePath (String): file path for the Bomber Monkey's idle sprite

monkeyBSHOOTspritePath (String): file path for the Bomber Monkey's shooting sprite

explosionSprite (String): file path for the explosion effect sprite
```
MonkeyC Class Variables:
```
COST (int): static constant for the base cost of a Slow Monkey

monkeyCIDLEspritePath (String): file path for the Slow Monkey's idle sprite

monkeyCSHOOTspritePath (String): file path for the Slow Monkey's shooting sprite

iceExplosionspritePath (String): file path for the ice explosion sprite

slowDurationMillis (int): duration (in milliseconds) for which humans are slowed by this monkey's projectiles
```
Projectile Class Variables:
```
x, y (double): coordinates of the projectile

dx, dy (double): change in X and Y coordinates per update, representing movement direction

radius (int): visual radius of the projectile

color (Color): color of the projectile

speed (double): speed of the projectile

damage (int): damage dealt by the projectile

target (Human): human object this projectile is targeting

isExplosive (boolean): true if the projectile explodes on impact

aoeRadius (double): radius of the Area of Effect (AoE) for explosive projectiles

isSlowingProjectile (boolean): true if the projectile applies a slow effect

slowDurationMillis (int): duration of the slow effect applied by this projectile

currentState (enum State): current state of the projectile (active, exploding, spent)

explosionAgeTicks (int): counter for how long the explosion visual has been active

explosionDurationTicks (int): total duration of the explosion visual

explosionCenterX, explosionCenterY (double): center coordinates of the explosion

explosionSprite (BufferedImage): image used for the explosion effect

sprite (BufferedImage): image used for the projectile itself while flying

defaultSpritePath (String): default sprite path for non-explosive, non-slowing projectiles

bombSpritePath (String): sprite path for explosive projectiles

slowSpritePath (String): sprite path for slowing projectiles

hitThreshold (double): distance threshold to consider a projectile to have hit its target

SideInfoPanel Class Variables:

gameState (GameState): refers to the GameState object

gamePanel (GamePanel): refers to the GamePanel object

panelWidth (int): static constant for the preferred width of the panel

buttonHeight (int): static constant for the height of buttons

buySpritePreviewSize (int): size for the preview sprites on buy buttons

buyButtonWidth (int): width for the buy buttons

defaultButtonColor (Color): default background color for buttons

hoverButtonColor (Color): background color for buttons when hovered over

pressedButtonColor (Color): background color for buttons when pressed

disabledButtonColor (Color): background color for disabled buttons

buttonTextColor (Color): text color for buttons

waveStatusLabel (JLabel): label displaying the current wave status

startGameButton (JButton): button to start the game or the next wave

livesLabel (JLabel): label displaying the current number of lives
```
SpriteManager Class Variables:
```
spriteCache (Map<String, BufferedImage>): cache to store loaded sprites, preventing redundant loading

placeholderSprite (BufferedImage): placeholder image used when a requested sprite cannot be loaded
```
UpgradeControlPanel Class Variables:
```
upgradeGUI_instance (UpgradeGUI): an instance of UpgradeGUI to handle the drawing and logic of the upgrade interface

currentSelectedMonkey (Monkey): monkey currently selected by the user, whose upgrade options are displayed

gameState (GameState): refers to the GameState object

gamePanel_ref (GamePanel): refers to the GamePanel object, used for selling monkeys or repainting

panelWidth (int): static constant for the preferred width of the panel
```
UpgradeGUI Class Variables:
```
actionNONE, actionUpgradedOrArchetypeChosen, actionSOLD (int): constants representing different actions resulting from a click

upgradeButtonBounds (Rectangle): defines the clickable area for the standard upgrade button

archetype1ButtonBounds (Rectangle): defines the clickable area for the first archetype button

archetype2ButtonBounds (Rectangle): defines the clickable area for the second archetype button

sellButtonBounds (Rectangle): defines the clickable area for the sell button

buttonWidth, archetypebuttonWidth, sellbuttonWidth (int): widths for different types of buttons

buttonHeight, archetypebuttonHeight, sellbuttonHeight (int): heights for different types of buttons

padding (int): general spacing used for layout

sectionSpacing (int): vertical spacing between different sections of the GUI

tooltipSpacingAfterSell (int): vertical spacing specifically after the sell button to place the tooltip

defaultButtonColor, hoverButtonColor, disabledButtonColor, disabledHoverButtonColor (Color): color schemes for upgrade/archetype buttons

defaultSellButtonColor, hoverSellButtonColor (Color): color schemes for the sell button

currentButtonColor, currentArchetype1Color, currentArchetype2Color, currentSellButtonColor (Color): variables to store the dynamically updated colors of the buttons based on hover and affordability

buttonBorderColor (Color): color for button borders

buttonTextColor (Color): text color for buttons

buttonFont, headerFont, statsFont, costFont, tooltipFont, sellButtonFont (Font): different font styles used for text elements

currentTooltipText (String): text displayed as a tooltip when hovering over a button
```
SpawnInstruction Inner Class Variables (within GamePanel):
```
humanType (String): type of human (enemy) to spawn (eg, "normal", "kid", "bossbaby")

count (int): number of units of this type to spawn

delayTicksAfterPreviousGroup (int): delay in game ticks before this group starts spawning, after the previous group

intervalTicksPerUnit (int): delay in game ticks between spawning individual units within this group

isCamo (boolean): boolean for if the human type is camo
```
WaveDefinition Inner Class Variables (within GamePanel):
```
spawns (List): list of SpawnInstruction objects defining the composition of the wave
```

## Types of Methods

### This section categorizes and briefly describes the types of methods implemented across the project's classes

Constructor Methods:

    Initialize new objects of a class, setting up their initial state and dependencies

Game Logic and Update Methods:

    Implement the core game mechanics, including movement, targeting, attacking, and state transitions These methods are called repeatedly within the game loop

Drawing and Rendering Methods:

    Responsible for drawing the visual elements of the game onto the screen This includes paintComponent overrides and helper methods for drawing specific UI elements or game entities

Event Handling Methods:

    Respond to user input, such as mouse clicks and movements, to trigger game actions These methods often involve MouseListener and MouseMotionListener implementations

Setter Methods:

    Modify the internal state of objects or the game, often related to resources, health, or progression

Getter Methods:

    Provide read-only access to the internal state or properties of an object

Utility and Helper Methods:

    Perform specific tasks that support the main logic, such as calculations, resource loading, specific object creation, or general-purpose computations

Upgrade and Archetype Methods:

    Manage the progression and specialization of game entities, particularly monkeys This includes applying stat changes and handling the selection of upgrade paths

UI Component Styling and Creation Methods:

    Methods specifically designed to style and create Swing UI components, often involving setting colors, fonts, borders, and layout properties


Rationale

The project is structured using Swing for the graphical user interface, leveraging JPanel for the main game display and Timer for the game loop MainFrame acts as the primary application window, orchestrating the various panels. Separation of concerns is achieved by having GamePanel manage rendering and interaction, while GameState encapsulates the core game variables and their manipulation Human and Monkey classes define the core entities with their own update and draw logic Map class provides the pathfinding data This modular design aims for maintainability and clarity use of MouseAdapter and MouseMotionAdapter simplifies event handling for user input Wave management is designed to support both pre-defined and procedurally generated waves, allowing for extensible gameplay Monkeys have a clear upgrade path system with archetypes to introduce strategic depth, and specialized monkey types like MonkeyB and MonkeyC extend the base functionality Projectile class handles the behavior of shots fired by monkeys Dedicated UI panels (SideInfoPanel, UpgradeControlPanel) improve user experience by providing clear information and interactive elements SpriteManager centralizes image loading and caching for efficient resource management UpgradeGUI specifically encapsulates the visual and interaction logic for the upgrade interface, making UpgradeControlPanel cleaner
Dependencies

This project is written in Java and relies on the following standard Java libraries:

    javaxswing*: For GUI components and event handling

    javaawt*: For graphics, drawing, and basic UI elements (eg, Color, Point, Rectangle, Graphics2D, Font, FontMetrics, BasicStroke, AlphaComposite, Composite, Image, MouseInfo)

    javaawtevent*: For event handling (mouse, action)

    javaawtimageBufferedImage: For image handling

    javaawtgeomAffineTransform: For applying transformations like rotation to graphics

    javaawtgeomLine2DDouble: For geometric calculations related to path segments

    javautil*: For data structures like ArrayList, List, Iterator, Random, HashMap, Map

    javaioIOException, javaioInputStream: For input/output operations, particularly for image loading

    javaximageioImageIO: For reading image files

Files Used

    MainFramejava: main entry point of the application, responsible for setting up the JFrame and integrating all other panels

    GamePaneljava: Contains the GamePanel class, responsible for game rendering, logic, and user interaction

    GameStatejava: Contains the GameState class, managing the core numerical state of the game

    Humanjava: Defines the Human class, representing enemy units with their movement, health, and visual properties

    Mapjava: Defines the Map class, managing the game's path for human movement

    Monkeyjava: Defines the Monkey class, the base class for all monkey towers, including their targeting, shooting, and upgrade logic

    MonkeyBjava: Defines the MonkeyB class, a specialized monkey type that extends Monkey with explosive projectile capabilities

    MonkeyCjava: Defines the MonkeyC class, a specialized monkey type that extends Monkey with slowing projectile capabilities

    Projectilejava: Defines the Projectile class, representing shots fired by monkeys, handling their movement, impact, and visual effects

    SideInfoPaneljava: Defines the SideInfoPanel class, displaying general game information (money, lives, wave) and buy buttons for monkeys

    SpriteManagerjava: Defines the SpriteManager class, responsible for loading, caching, and scaling image sprites

    UpgradeControlPaneljava: Defines the UpgradeControlPanel class, providing a UI for upgrading and selling selected monkeys

    UpgradeGUIjava: Defines the UpgradeGUI class, which handles the drawing and interaction logic for the monkey upgrade interface within the UpgradeControlPanel

    map_background.png): Image file for the game map background

    human_baby.png, human_kid0.png, human_kid1.png, human_normal0.png, human_normal1.png, human_normal2.png, human_bodybuilder.png, human_businessman.png, human_bigbaby.png, human_ninja.png, human_bossninja.png: Image files for various human sprites

    monkey_base_idle.png, monkey_base_shoot.png : Image files for base monkey sprites

    monkey_bomber_idle.png, monkey_bomber_shoot.png : Image files for Bomber Monkey sprites

    monkey_slow_idle.png, monkey_slow_shoot.png: Image files for Slow Monkey sprites

    explosion_effect.png: Image file for the explosion effect (used by MonkeyB)

    project_slow.png: Image file for the slow projectile/explosion effect (used by MonkeyC and Projectile)

    projectile_dart.png: Default image file for projectiles

    projectile_bomb.png: Image file for bomb projectiles

Compilation and Execution

This project is a Java Swing application

Compile and Execute:
After successful compilation, run the application by executing the MainFrame class, which contains the main method

java MainFrame
