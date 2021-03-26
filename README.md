# sunlight computation
sunlight computation for simple buildings
### core
core primitive and calculator

#### Sun
sun calculator for any location, date or time with algorithm accuracy < 1°
#### Shadow
shadow calculator for the sun at a given position
- calculate shadow at the current time
- calculate all-day shadow on a given date
#### Building
building for sunlight computation
- create from a base polygon and height
- create from a HE_Mesh
#### Scene
scene with a camera, sun, control panel and buildings
#### DurationAnalysis
analysis on sunlight duration at a give point in the scene
- point analysis
- grid analysis
### utility
toolkit for geometric operation and rendering
#### CtrlPanel
the second PApplet window for controllers
#### JtsRender
renderer for JTS primitives
#### PolyWithNormal
polygon with normal to check if the front is outward
#### PolyHandler
convert between JTS Polygon and HE_Mesh WB_Polygon
#### IOHandler
handle input/output issues
### test
test programs
