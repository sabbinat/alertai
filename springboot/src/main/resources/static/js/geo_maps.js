document.addEventListener('DOMContentLoaded', () => {
  // -----------------------------
  // Modo CREACIÓN: mapa en "map-form"
  // -----------------------------
  const createMapDiv = document.getElementById('map-form');

  if (createMapDiv) {
    const createMap = L.map('map-form').setView([-30.9059, -55.5501], 13);

    createMapDiv._leaflet_map_instance = createMap;


    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(createMap);

    // Fuerza ajuste cuando se muestra el modal
    const modalCrear = document.getElementById('crearEventoModal');
    modalCrear.addEventListener('shown.bs.modal', function () {
      if (createMapDiv._leaflet_map_instance) {
        createMapDiv._leaflet_map_instance.invalidateSize();
      }
    });


    let marker;

    window.geocodeAddress = function () {
      const address = document.getElementById('address').value;

      fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
        .then(response => response.json())
        .then(data => {
          if (data && data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lon = parseFloat(data[0].lon);
            
            // Eliminar marcador anterior si existe
            if (marker) createMap.removeLayer(marker);

            marker = L.marker([lat, lon], { draggable: true }).addTo(createMap)
              .bindPopup('Ubicación encontrada').openPopup();

            // Centra el mapa
            createMap.setView([lat, lon], 15);

            // Establece coordenadas en campos ocultos
            document.getElementById('latitude').value = lat;
            document.getElementById('longitude').value = lon;

            // Actualiza las coordenadas si se arrastra el marcador
            marker.on('dragend', function (e) {
              const newLatLng = e.target.getLatLng();
              document.getElementById('latitude').value = newLatLng.lat;
              document.getElementById('longitude').value = newLatLng.lng;
            });
          } else {
            alert('Dirección no encontrada.');
          }
        })
        .catch(err => {
          alert('Error al buscar la dirección');
          console.error(err);
        });
    };
  }

  // -----------------------------
  // Modo EDICIÓN: mapa en "map-edit-{event.id}"
  // -----------------------------
  const editMapDiv = document.querySelector('[id^="map-edit-"]');
  if (editMapDiv) {
    const eventId = editMapDiv.dataset.eventId;
    const lat = parseFloat(document.getElementById(`latitude-edit-${eventId}`).value || -30.9059);
    const lon = parseFloat(document.getElementById(`longitude-edit-${eventId}`).value || -55.5501);

    const editMap = L.map(`map-edit-${eventId}`).setView([lat, lon], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(editMap);

    let editMarker = L.marker([lat, lon], { draggable: true }).addTo(editMap);

    // Actualizar coordenadas al mover marcador
    editMarker.on('dragend', function (e) {
      const newLatLng = e.target.getLatLng();
      document.getElementById(`latitude-edit-${eventId}`).value = newLatLng.lat;
      document.getElementById(`longitude-edit-${eventId}`).value = newLatLng.lng;
    });

    window.geocodeAddressEdit = function (id) {
      const address = document.getElementById(`address-edit-${id}`).value;

      fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
        .then(response => response.json())
        .then(data => {
          if (data && data.length > 0) {
            const lat = parseFloat(data[0].lat);
            const lon = parseFloat(data[0].lon);

            editMap.setView([lat, lon], 15);
            editMap.removeLayer(editMarker);

            editMarker = L.marker([lat, lon], { draggable: true }).addTo(editMap)
              .bindPopup('Ubicación actualizada').openPopup();

            document.getElementById(`latitude-edit-${id}`).value = lat;
            document.getElementById(`longitude-edit-${id}`).value = lon;

            editMarker.on('dragend', function (e) {
              const newLatLng = e.target.getLatLng();
              document.getElementById(`latitude-edit-${id}`).value = newLatLng.lat;
              document.getElementById(`longitude-edit-${id}`).value = newLatLng.lng;
            });
          } else {
            alert('Dirección no encontrada.');
          }
        })
        .catch(err => {
          alert('Error al buscar la dirección');
          console.error(err);
        });
    };
  }
});
