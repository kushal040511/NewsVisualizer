import SmoothScroll from "@/components/SmoothScroll";
import LandingHero from "@/components/landing/LandingHero";
import LandingNav from "@/components/landing/LandingNav";
import LandingMarquee from "@/components/landing/LandingMarquee";
import LandingModules from "@/components/landing/LandingModules";
import LandingFooter from "@/components/landing/LandingFooter";

export default function Home() {
  return (
    <SmoothScroll>
      <div className="noise">
        <LandingNav />
        <main>
          <LandingHero />
          <LandingMarquee />
          <LandingModules />
        </main>
        <LandingFooter />
      </div>
    </SmoothScroll>
  );
}
