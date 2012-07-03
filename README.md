WanderrouteJS
=============
A little university groupe-project, which aims to display tracking routes with WebGL

Feature Overview:
- generate a terrain-mesh from NASA SRTM data
  - extract a heightmap image from the SRTM file
  - displace a uniform grid with the hightmap
  - generate normal and ambient occlusion map
- read OpenStreetMap track data and transform to renderable data 
  - 3D prisma of the path
  - line mesh
- automatic builder(input a OSM track)
  - download all needed SRTM tiles
  - generate all the needed resources for rendering


PS:
to build the WebTerrainGeneration project you first need to build the Darwin libs from:
https://github.com/Danny02/DarwinsBox/