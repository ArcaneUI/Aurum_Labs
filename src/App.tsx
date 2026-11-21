import Navbar from "./components/Navbar2";
import Hero2 from "./components/Hero2";
import CustomScrollbar from "./components/CustomScrollbar";
import ModCatalog from "./components/ModCatalog";
import InstallHub from "./components/InstallHub";
import Footer2 from "./components/Footer2";

export default function App() {
  return (
    <div className="relative min-h-screen bg-ink text-white">
      <CustomScrollbar />
      <Navbar />
      <main>
        <Hero2 />
        <ModCatalog />
        <InstallHub />
      </main>
      <Footer2 />
    </div>
  );
}
