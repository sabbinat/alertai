document.addEventListener("DOMContentLoaded", function () {

  try {
        const parsedEvents = JSON.parse(calendarEvents);
      let calendar = document.querySelector('.calendar');
      const month_names = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

      const isLeapYear = (year) => {
      return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
      };

      const getFebDays = (year) => {
      return isLeapYear(year) ? 29 : 28;
      };

      const generateCalendar = (month, year) => {
      let calendar_days = calendar.querySelector('.calendar-days');
      let calendar_header_year = calendar.querySelector('#year');
      let month_picker = calendar.querySelector('#month-picker');

      let days_of_month = [31, getFebDays(year), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
      calendar_days.innerHTML = '';

      if (month === undefined) month = currDate.getMonth();
      if (year === undefined) year = currDate.getFullYear();
      

      let curr_month = month_names[month];
      month_picker.innerHTML = curr_month;
      calendar_header_year.innerHTML = year;

      let first_day = new Date(year, month, 1);

      for (let i = 0; i <= days_of_month[month] + first_day.getDay() - 1; i++) {
          let day = document.createElement('div');

          if (i >= first_day.getDay()) {
          const dateNum = i - first_day.getDay() + 1;
          const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(dateNum).padStart(2, '0')}`;

          day.classList.add('calendar-day-hover');
          day.innerHTML = `<div class="day-number">${dateNum}</div>`;

          const eventsForDay = parsedEvents.filter(event => event.start === dateStr);
          if (eventsForDay.length > 0) {
              day.classList.add('event-day');
              day.setAttribute('data-event-date', dateStr);
              day.addEventListener('click', () => {

              // Ir al primer evento del día en el mapa
              const event = eventsForDay[0];
              if (event.latitude && event.longitude) {
                  map.setView([event.latitude, event.longitude], 13); // Zoom 13
                  const popupContent = `<strong>${event.title}</strong><br>${event.description || ''}`;
                  L.popup()
                  .setLatLng([event.latitude, event.longitude])
                  .setContent(popupContent)
                  .openOn(map);
              }
              });

          }

          if (
              dateNum === currDate.getDate() &&
              year === currDate.getFullYear() &&
              month === currDate.getMonth()
          ) {
              day.classList.add('curr-date');
          }
          }

          calendar_days.appendChild(day);
      }
      };

      // Inicializar calendario
      let currDate = new Date();
      let currentMonth = currDate.getMonth();
      let currentYear = currDate.getFullYear();
      generateCalendar(currentMonth, currentYear);

      document.getElementById("prev-month").addEventListener("click", () => {
      currentMonth--;
      if (currentMonth < 0) {
          currentMonth = 11;
          currentYear--;
      }
      generateCalendar(currentMonth, currentYear); // usa tu función
      });

      document.getElementById("next-month").addEventListener("click", () => {
      currentMonth++;
      if (currentMonth > 11) {
          currentMonth = 0;
          currentYear++;
      }
      generateCalendar(currentMonth, currentYear); 
      });

      // Navegación
      let year_text = document.querySelector('#year');
      let prev_year = document.querySelector('#prev-year');
      let next_year = document.querySelector('#next-year');

      prev_year.addEventListener('click', () => {
      currentYear--;
      generateCalendar(currentMonth, currentYear);
      });

      next_year.addEventListener('click', () => {
      currentYear++;
      generateCalendar(currentMonth, currentYear);
      });

      // MAPA GENERAL DE EVENTOS
      const map = L.map('event-map').setView([0, 0], 2);
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
      }).addTo(map);

      const bounds = [];

      parsedEvents.forEach(event => {
      if (event.latitude && event.longitude) {
          const marker = L.marker([event.latitude, event.longitude]).addTo(map);
          marker.bindPopup(`<strong>${event.title}</strong><br>${event.description || ''}`);
          bounds.push([event.latitude, event.longitude]);
      }
      });

      if (bounds.length > 0) {
      map.fitBounds(bounds, { padding: [30, 30] });
      }

  } catch (error) {
      console.error("Error parsing calendar events:", error);
  }
});
