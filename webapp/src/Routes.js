import DriveDetails from "./components/DriveDetails";
import Search from "./components/Search";

// Contains routes for all the pages
const Routes = [
    {
        path: '/',
        sidebarName: 'Browse Drive',
        component: DriveDetails
    },
    {
        path: '/search',
        sidebarName: 'Search',
        component: Search
    }
];

export default Routes;