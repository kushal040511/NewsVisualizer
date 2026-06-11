"use client";

import { Canvas, useFrame, useThree } from "@react-three/fiber";
import { Float, Environment, Lightformer } from "@react-three/drei";
import * as THREE from "three";
import { useMemo, useRef } from "react";

/**
 * The "signal core" — a wireframe globe wrapped in orbiting data points
 * and a neon halo. Reacts to cursor movement.
 */
function Core() {
  const group = useRef<THREE.Group>(null);
  const { pointer } = useThree();

  useFrame((state, delta) => {
    if (!group.current) return;
    group.current.rotation.y += delta * 0.18;
    group.current.rotation.x = THREE.MathUtils.damp(
      group.current.rotation.x,
      pointer.y * 0.3,
      3,
      delta
    );
    group.current.rotation.z = THREE.MathUtils.damp(
      group.current.rotation.z,
      -pointer.x * 0.2,
      3,
      delta
    );
  });

  // Scatter points on the sphere surface — news events on the globe
  const points = useMemo(() => {
    const positions = new Float32Array(420 * 3);
    for (let i = 0; i < 420; i++) {
      const phi = Math.acos(2 * ((i * 0.6180339887) % 1) - 1);
      const theta = Math.PI * (1 + Math.sqrt(5)) * i;
      const r = 1.32;
      positions[i * 3] = r * Math.sin(phi) * Math.cos(theta);
      positions[i * 3 + 1] = r * Math.cos(phi);
      positions[i * 3 + 2] = r * Math.sin(phi) * Math.sin(theta);
    }
    return positions;
  }, []);

  return (
    <group ref={group}>
      {/* Inner solid sphere — dark glass */}
      <mesh>
        <sphereGeometry args={[1.12, 64, 64]} />
        <meshPhysicalMaterial
          color="#0a0a0c"
          metalness={0.9}
          roughness={0.25}
          clearcoat={1}
          clearcoatRoughness={0.2}
        />
      </mesh>

      {/* Wireframe lattice */}
      <mesh>
        <icosahedronGeometry args={[1.32, 2]} />
        <meshBasicMaterial
          color="#00f0ff"
          wireframe
          transparent
          opacity={0.16}
        />
      </mesh>

      {/* Event points */}
      <points>
        <bufferGeometry>
          <bufferAttribute
            attach="attributes-position"
            args={[points, 3]}
          />
        </bufferGeometry>
        <pointsMaterial
          color="#00f0ff"
          size={0.025}
          transparent
          opacity={0.85}
          sizeAttenuation
        />
      </points>

      {/* Equatorial ring */}
      <mesh rotation={[Math.PI / 2.4, 0, 0.3]}>
        <torusGeometry args={[1.75, 0.005, 8, 128]} />
        <meshBasicMaterial color="#00f0ff" transparent opacity={0.5} />
      </mesh>
      <mesh rotation={[Math.PI / 1.8, 0.4, -0.4]}>
        <torusGeometry args={[1.95, 0.003, 8, 128]} />
        <meshBasicMaterial color="#ffffff" transparent opacity={0.18} />
      </mesh>
    </group>
  );
}

export default function CoreScene() {
  return (
    <Canvas
      camera={{ position: [0, 0.3, 5.4], fov: 36 }}
      dpr={[1, 2]}
      gl={{ antialias: true, alpha: true }}
      style={{ background: "transparent" }}
      aria-label="Rotating 3D globe of live news signals"
    >
      <ambientLight intensity={0.3} />
      <spotLight position={[6, 6, 4]} intensity={50} angle={0.5} penumbra={1} />
      <pointLight position={[-4, -2, -3]} intensity={12} color="#00f0ff" />

      <Float speed={1.2} rotationIntensity={0.2} floatIntensity={0.6}>
        <Core />
      </Float>

      <Environment resolution={256}>
        <Lightformer
          intensity={3}
          position={[0, 4, 0]}
          rotation={[Math.PI / 2, 0, 0]}
          scale={[8, 4, 1]}
        />
        <Lightformer
          intensity={2}
          color="#00f0ff"
          position={[4, -1, -2]}
          rotation={[0, -Math.PI / 3, 0]}
          scale={[3, 6, 1]}
        />
      </Environment>
    </Canvas>
  );
}
