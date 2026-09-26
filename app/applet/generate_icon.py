svg_content = '''<?xml version="1.0" encoding="UTF-8"?>
<svg width="1024" height="1024" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
  <defs>
    <!-- Outer Glow -->
    <radialGradient id="bgGlow" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#1E293B" stop-opacity="0.8"/>
      <stop offset="60%" stop-color="#0F172A" stop-opacity="0.9"/>
      <stop offset="100%" stop-color="#080A10" stop-opacity="1"/>
    </radialGradient>

    <!-- Outer Ring Gradient -->
    <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#FFD000"/>
      <stop offset="25%" stop-color="#FF8C00"/>
      <stop offset="50%" stop-color="#8A2BE2"/>
      <stop offset="85%" stop-color="#00D2FF"/>
      <stop offset="100%" stop-color="#0066FF"/>
    </linearGradient>

    <!-- Inner Sphere Orange Glow -->
    <radialGradient id="orangeGlow" cx="35%" cy="30%" r="65%">
      <stop offset="0%" stop-color="#FFE680"/>
      <stop offset="35%" stop-color="#FF9000"/>
      <stop offset="70%" stop-color="#D94800"/>
      <stop offset="100%" stop-color="#801000"/>
    </radialGradient>

    <!-- Inner Sphere Blue Glow -->
    <radialGradient id="blueGlow" cx="65%" cy="70%" r="65%">
      <stop offset="0%" stop-color="#80E5FF"/>
      <stop offset="30%" stop-color="#0088FF"/>
      <stop offset="70%" stop-color="#0033B3"/>
      <stop offset="100%" stop-color="#050B30"/>
    </radialGradient>

    <!-- Highlight Specular -->
    <linearGradient id="highlightGrad" x1="20%" y1="0%" x2="80%" y2="100%">
      <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.8"/>
      <stop offset="50%" stop-color="#E2D0FF" stop-opacity="0.3"/>
      <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0.0"/>
    </linearGradient>

    <!-- Glass Rim Highlight -->
    <linearGradient id="glassRim" x1="0%" y1="100%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#00F0FF" stop-opacity="0.9"/>
      <stop offset="50%" stop-color="#FFFFFF" stop-opacity="0.2"/>
      <stop offset="100%" stop-color="#FFAA00" stop-opacity="0.9"/>
    </linearGradient>

    <filter id="blurGlow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="25"/>
    </filter>
  </defs>

  <!-- Background -->
  <rect width="1024" height="1024" fill="#080A10"/>

  <!-- Center Ambient Glow -->
  <circle cx="512" cy="512" r="460" fill="url(#bgGlow)"/>

  <!-- Outer Ring (Glass Ring) -->
  <circle cx="512" cy="512" r="390" fill="none" stroke="url(#ringGrad)" stroke-width="18" opacity="0.95"/>
  <circle cx="512" cy="512" r="390" fill="none" stroke="url(#ringGrad)" stroke-width="36" opacity="0.35" filter="url(#blurGlow)"/>

  <!-- Main Sphere Container -->
  <g>
    <!-- Base Sphere Shadow / Dark Backing -->
    <circle cx="512" cy="512" r="320" fill="#0A0E26"/>

    <!-- Blue Liquid Half (Bottom-Right) -->
    <path d="M 512 192 A 320 320 0 1 1 192 512 C 320 620 620 320 512 192 Z" fill="url(#blueGlow)"/>

    <!-- Orange Liquid Half (Top-Left Swirl) -->
    <path d="M 512 192 A 320 320 0 0 0 192 512 C 300 400 450 650 832 512 A 320 320 0 0 0 512 192 Z" fill="url(#orangeGlow)"/>

    <!-- Fluid Wave Divider (Translucent Swirl Layer) -->
    <path d="M 230 450 C 380 320 650 680 790 580 C 720 720 400 680 230 450 Z" fill="#FFAA00" opacity="0.4" filter="url(#blurGlow)"/>
    <path d="M 280 380 C 420 280 620 580 780 480 C 650 600 420 520 280 380 Z" fill="#00D2FF" opacity="0.4" filter="url(#blurGlow)"/>

    <!-- Fluid Curve Accent Line -->
    <path d="M 220 480 Q 400 300 550 480 T 820 490" fill="none" stroke="#FFFFFF" stroke-width="8" opacity="0.75" stroke-linecap="round"/>
    <path d="M 220 480 Q 400 300 550 480 T 820 490" fill="none" stroke="#E2D0FF" stroke-width="20" opacity="0.4" stroke-linecap="round" filter="url(#blurGlow)"/>

    <!-- Glass Sphere Overlay Rim & Highlight -->
    <circle cx="512" cy="512" r="320" fill="url(#highlightGrad)"/>
    <circle cx="512" cy="512" r="318" fill="none" stroke="url(#glassRim)" stroke-width="6" opacity="0.8"/>
  </g>
</svg>
'''

with open('/app/applet/generate_icon.svg', 'w') as f:
    f.write(svg_content)

print("SVG created successfully")
