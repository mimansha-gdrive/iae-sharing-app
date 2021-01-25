import './App.css';
import { Switch, Route } from 'react-router-dom';
import Routes from './Routes'
import React from "react";
import NavBar from "./components/NavBar";

function App() {
  return (
      <div>
          <NavBar />

          <Switch>
              {Routes.map((route) => (
                  <Route exact path={route.path} key={route.path}>
                      <route.component />
                  </Route>
              ))}
          </Switch>
      </div>
  );
}

export default App;
