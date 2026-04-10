<template>
  <div class="map-container">
    <div id="map"></div>
  </div>
</template>

<script>
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

export default {
  name: 'MapPage',
  mounted() {
    this.initMap()
  },
  methods: {
    initMap() {
      const map = L.map('map').setView([51.505, -0.09], 13)

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
      }).addTo(map)

      // Hardcoded marker
      L.marker([51.5, -0.09])
        .addTo(map)
        .bindPopup('Challenge Location')
        .openPopup()
    }
  }
}
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100vh;
  display: flex;
}

#map {
  flex: 1;
}
</style>
