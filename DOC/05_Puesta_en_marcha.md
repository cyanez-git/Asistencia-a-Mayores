# Puesta en marcha (etapa 1)
**Inicio:** 2026-11-17
**Base:** [04_Hardware.md](04_Hardware.md)

Registro de lo instalado y configurado en el banco de pruebas, con lo aprendido en cada paso. No se registran claves ni códigos de acceso.

---

## 1. Equipo central: Home Assistant OS en máquina virtual

**Equipo:** mini PC Bangho Cubic A4130 con Ubuntu instalado, conectada por cable al router.

**Por qué en máquina virtual y no instalado directo:**
- La BIOS tiene **Secure Boot activado** y **no ofrece ninguna opción para desactivarlo** (verificado con `mokutil --sb-state`).
- Home Assistant OS no tiene su arranque firmado para Secure Boot, así que no arranca instalado directo en el disco.
- Un pendrive con Ubuntu 26.04 tampoco arrancaba: la BIOS (circa 2014) solo reconoce las firmas con el certificado de Microsoft de 2011; las versiones nuevas usan el de 2023.
- Solución: Ubuntu (que sí arranca) como sistema base y Home Assistant OS como máquina virtual KVM, con firmware UEFI virtual **sin** Secure Boot.

**Aprendizaje para el producto:** el equipo central definitivo debe permitir desactivar Secure Boot o venir preparado; verificarlo antes de elegir hardware.

**Instalación (Ubuntu):**

```bash
sudo apt update && sudo apt install -y qemu-system-x86 libvirt-daemon-system virtinst ovmf xz-utils
cd /tmp && wget https://github.com/home-assistant/operating-system/releases/download/<versión>/haos_ova-<versión>.qcow2.xz
unxz /tmp/haos_ova-*.qcow2.xz && sudo mv /tmp/haos_ova-*.qcow2 /var/lib/libvirt/images/haos.qcow2
sudo virt-install --name haos --os-variant generic --memory 4096 --vcpus 2 --import \
  --disk /var/lib/libvirt/images/haos.qcow2,bus=sata \
  --boot uefi,firmware.feature0.name=secure-boot,firmware.feature0.enabled=no \
  --network type=direct,source=<placa de red con cable>,source_mode=bridge,model=virtio \
  --graphics none --autostart --noautoconsole
```

**Estado:** máquina virtual `haos` en ejecución, con arranque automático. 4 GB de RAM y 2 núcleos. Home Assistant responde en el puerto 8123 y el usuario administrador está creado.

**Notas:**
- La red de la máquina virtual es "directa" (macvtap): Home Assistant recibe su propia IP del router. Desde la propia mini PC no se puede entrar a Home Assistant; desde cualquier otro equipo de la red, sí.
- La IP la asigna el router por DHCP. ⚠ Conviene reservarla en el router para que no cambie.
- Ver estado: `sudo virsh list`.

## 2. Reloj: datos de sensores a Home Assistant ✅ (2026-11-17)

**Resultado:** el reloj aparece en Home Assistant como dispositivo **"Reloj (reloj1)"** con sus sensores (pulso, intervalo entre latidos, temperatura de piel y ambiente, aceleración). Los datos viajan por WiFi directo al broker MQTT de la casa, **sin pasar por la nube de Samsung**. Objetivo de la etapa cumplido.

**Equipo:** Galaxy Watch5 SM-R900 (40 mm), One UI Watch 8 / Wear OS 6 (API 36). Servicio de sensores de Samsung ("Plataforma de Salud", paquete `com.samsung.android.service.health`) versión 1.7.00.05. SDK: Samsung Health Sensor SDK 1.4.1.

**App:** [`reloj/app`](../reloj/app). Servicio en primer plano que lee `HEART_RATE_CONTINUOUS`, `SKIN_TEMPERATURE_CONTINUOUS` y `ACCELEROMETER_CONTINUOUS` y publica un resumen cada 5 s por MQTT, con "MQTT discovery" para que Home Assistant cree los sensores solo. El SDK (`.aar`) no se sube al repositorio: se copia desde `reloj/sdk`.

**Home Assistant:** app "Mosquitto broker" instalada; usuario propio para el reloj.

**Configuración del reloj** (la carga el usuario desde la notebook, la clave no pasa por el repositorio):

```
adb shell am start -n ar.asistencia.reloj/.presentation.MainActivity --es host <IP de HA> --es usuario <usuario> --es clave <clave>
```

**Aprendizajes (no estaban claros en la documentación):**
- **Modo desarrollador de Samsung** (sin él: `SDK_POLICY_ERROR`): en el reloj, Ajustes → Aplicaciones → **Plataforma de Salud** (así se llama en español en esta versión, no "Health Sensor Service"). Tocar repetidamente hasta que aparezca el mensaje "Los datos de los sensores se compartirán con las aplicaciones conectadas" y la etiqueta **[Dev mode]**. No aparece ningún botón ni licencia. Se verifica en el log del servicio: `isDeveloperMode true/false`.
- **Permisos en Wear OS 6:** `BODY_SENSORS` ya no aplica. Hacen falta `android.permission.health.READ_HEART_RATE`, `android.permission.health.READ_SKIN_TEMPERATURE` (sin este: `PERMISSION_ERROR` en temperatura) y `com.samsung.android.hardware.sensormanager.permission.READ_ADDITIONAL_HEALTH_DATA`.
- **Servicio de salud en primer plano:** Android solo permite iniciarlo con la app abierta y visible. Se inicia con el botón "Iniciar"; no puede arrancar solo desde segundo plano.
- **Depuración por WiFi:** el reloj la apaga cuando suspende el WiFi; al reactivarla cambia el puerto. El vínculo con la notebook se mantiene. Conviene trabajar con el reloj cargando y la pantalla encendida.
- **Acelerómetro:** llega a unas 60 muestras por segundo (no 25), en unidades crudas; reposo ≈ 4100 (≈ 1 g ≈ 4096). A calibrar.

**Pendiente del reloj:**
- ~~Confirmar valores de pulso y temperatura con el reloj bien puesto.~~ **Confirmado:** los valores en Home Assistant coinciden con los del reloj.
- Detección de caídas en el reloj a partir del acelerómetro.
- Botón SOS desde el reloj.
- Inicio automático tras reiniciar el reloj (requiere una forma permitida por Android 16).

## 3. Pendientes generales

1. Pasar la mini PC al mismo router que las cámaras y la alarma (hoy están en redes distintas).
2. Reservar la IP de Home Assistant en el router.
3. Integrar las cámaras Seisa e Imou.
4. Cuando lleguen: placa de relés (ESPHome), coordinador Zigbee y sensores.
