import React, {useState} from 'react'
import Button from "@material-ui/core/Button";
import {Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles({
    table: {
        minWidth: 250,
        maxWidth: 500
    },
});

const Search = () => {
    let [searchTerm, setSearchTerm] = useState(null);
    let [searchResult, setSearchResult] = useState(null);

    const updateTerm = (e) => {
        setSearchTerm(e.target.value);
    }

    const handleSearch = () => {
        async function search() {
            if (searchTerm) {
                const response = await fetch(`http://localhost:8080/api/v1/search?query=${searchTerm}`);
                return await response.json();
            }
        }

        search().then((json) => {
            setSearchResult(json);
        });
    }

    const classes = useStyles();

    return (
        <div className="SearchPage">
            <TextField id="standard-basic" label="Search Query" onChange={updateTerm}/>

            <Button variant="contained" color="primary" onClick={handleSearch}>
                Search
            </Button>

            {searchResult &&

            <TableContainer component={Paper} className="SearchResultTable">
                <Table className={classes.table} aria-label="simple table">
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell align="right">Type</TableCell>
                        </TableRow>
                    </TableHead>

                    <TableBody>
                        {searchResult.map((row) => (
                            <TableRow key={row.text}>
                                <TableCell component="th" scope="row">
                                    {row.text}
                                </TableCell>
                                <TableCell align="right">{row.type}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            }
        </div>
    )
}

export default Search;