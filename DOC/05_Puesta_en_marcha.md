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

## 2. Pendientes

1. Pasar la mini PC al mismo router que las cámaras y la alarma (hoy están en redes distintas).
2. Reservar la IP de Home Assistant en el router.
3. Integrar las cámaras Seisa e Imou.
4. Cuando lleguen: reloj (app propia + recepción de datos), placa de relés (ESPHome), coordinador Zigbee y sensores.
