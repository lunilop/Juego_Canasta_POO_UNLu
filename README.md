# Canasta — Proyecto Final POO

## Autor
- Luna Lopez

## Cátedra y Asignatura
- **Asignatura:** Programación Orientada a Objetos (POO)
- Universidad Nacional de Luján (UNLu)

Implementación multijugador del juego de cartas **Canasta** (2, 4 o 6 jugadores), desarrollada aplicando diseño orientado a objetos, separación por capas (**MVC**), notificación de cambios (**Observer**), comunicación en red mediante **RMI** (`LibreriaRMIMVC`), **JavaFX** para interfaz gráfica, **Swing/consola** para interfaz de texto y persistencia de partidas y ranking.

## De qué trata el juego
Los jugadores compiten en parejas/equipos (o individualmente en partidas de 2 jugadores) para alcanzar el puntaje objetivo configurado al inicio (por ejemplo, 5.000, 7.000 o 15.000 puntos).

- **Flujo de turno:** cada jugador roba 2 cartas del mazo o toma el pozo completo (cumpliendo las condiciones de apertura y de pozo limpio, cruzado o tapado), puede bajar combinaciones válidas o agregar cartas a juegos ya bajados en mesa (o deshacer las bajadas del turno actual), y finaliza descartando una carta o cerrando la mano cuando cumple los requisitos.
- **Reglas principales del modelo:** manejo de mazos (4 o 8 barajas según cantidad de jugadores), 3 rojos (bonificación o penalización según corresponda), 3 negros como tapón del pozo, comodines (`2` y `Joker`) que cruzan el pozo, apertura mínima según puntaje acumulado, canastas (limpia, sucia y oculta) y cierre de mano/partida.
- **Persistencia:** guarda el estado de la partida en curso para retomarla desde el servidor y mantiene un **TOP 5** de jugadores ganadores en el ranking.

## Interfaces y arquitectura
El proyecto separa el dominio (`modelo` y `modelo.persistencia`), la coordinación (`controlador`), las vistas (`vista.grafica` y `vista.consola`) y el arranque en red (`servidor` y `cliente`), manteniendo el mismo modelo sin modificarlo para cada interfaz.

## Ejecución en red en una misma PC
1. **Servidor:** ejecutar `LanzadorServidor` indicando la IP local (por ejemplo, `127.0.0.1`) y un puerto (por ejemplo, `8888`). Si existe una partida guardada, permite elegir si restaurarla o iniciar una nueva.
2. **Clientes:** habilitar *Allow multiple instances* en la configuración de ejecución de `LanzadorCliente` para abrir varias ventanas. Cada cliente se conecta a la IP y puerto del servidor (`127.0.0.1` y `8888`), elige un **puerto local libre** distinto para no chocar con otras instancias en la misma máquina y selecciona **Interfaz Gráfica** o **Consola**.

## Diagrama de clases
El diagrama de clases en Mermaid junto con las responsabilidades de cada clase se encuentra en **`diagramaDeClases.md`**.