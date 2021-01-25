import React, {useEffect, useState} from 'react';
import TreeView from '@material-ui/lab/TreeView';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import TreeItem from '@material-ui/lab/TreeItem';
import Button from '@material-ui/core/Button';
import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Grid} from "@material-ui/core";

const LIST_API_URL = 'http://localhost:8080/api/v1/list';
const SHARE_API_URL = 'http://localhost:8080/api/v1/change-owner';
const DEFAULT_SHARE_EMAIL = 'interviewtest2.m@gmail.com';

const LOCAL_STORE_SELECTED_GDRIVE_ITEM_ID = 'gdrive-file-selected-id';
const LOCAL_STORE_SELECTED_GDRIVE_ITEM_NAME = 'gdrive-file-selected-name';

const DriveDetails = () => {
    let [transformedList, setTransformedList] = useState([]);
    let [open, setOpen] = useState(false);

    const handleDialogOpen = () => {
        setOpen(true);
    };

    const handleDialogClose = () => {
        setOpen(false);
    };

    useEffect(() => {
        async function getList() {
            const response = await fetch(LIST_API_URL)
            return await response.json();
        }

        getList().then((json) => {
            setTransformedList(json);
        });
    }, []);

    // Create a TreeView using treeItems data passed in. This is the JSON data
    const DataTreeView = ({treeItems}) => {
        return (
            <TreeView
                defaultCollapseIcon={<ExpandMoreIcon/>}
                defaultExpandIcon={<ChevronRightIcon/>}
            >
                {getTreeItemsFromData(treeItems)}
            </TreeView>
        );
    };

    // NOTE: TreeView component has a bug where it does not allow state to be set within the handler of
    // onNodeSelect(), hence this hack of using local storage. We can add TTL on the local storage set/get to
    // avoid firing transfer owner on a previously selected (hence stored) value.
    const handleNodeSelect = (e, id, text) => {
        e.preventDefault();

        localStorage.setItem(LOCAL_STORE_SELECTED_GDRIVE_ITEM_ID, id);
        localStorage.setItem(LOCAL_STORE_SELECTED_GDRIVE_ITEM_NAME, text);
    }

    const handleShare = () => {
        const file = localStorage.getItem(LOCAL_STORE_SELECTED_GDRIVE_ITEM_ID);

        console.log('sharing file ' + file);

        const transferOwner = () => {
            fetch(`${SHARE_API_URL}?id=${file}&new-owner-email=${DEFAULT_SHARE_EMAIL}`)
                .then((res) => {
                    return res.json();
                })
                .then((data) => {
                    console.log(data.message);
                    alert(data.message);
                });
        }

        transferOwner();
        handleDialogClose();
    }

    // Function to massage the data and create treeItems out of it
    const getTreeItemsFromData = treeItems => {
        return treeItems.map(treeItemData => {
            let children = undefined;

            if (treeItemData.nodes && treeItemData.nodes.length > 0) {
                children = getTreeItemsFromData(treeItemData.nodes);
            }

            return (
                <TreeItem
                    key={treeItemData.id}
                    nodeId={treeItemData.id}
                    label={treeItemData.text}
                    children={children}
                    onLabelClick={(e) => handleNodeSelect(e, treeItemData.id, treeItemData.text)}
                />
            );
        });
    };

    return (
        <Grid container spacing={3} className="TreeView">
            <Grid item xs={6}>
                <DataTreeView treeItems={transformedList}/>

                {/*  TRANSFER OWNERSHIP UI  */}
                <Button variant="contained" color="primary" onClick={handleDialogOpen} style={{marginTop: 20}}>
                    Transfer ownership
                </Button>

                {localStorage.getItem(LOCAL_STORE_SELECTED_GDRIVE_ITEM_ID) && <Dialog
                    open={open}
                    onClose={handleDialogClose}
                    aria-labelledby="alert-dialog-title"
                    aria-describedby="alert-dialog-description"
                >
                    <DialogTitle id="alert-dialog-title">{"Transfer ownership?"}</DialogTitle>
                    <DialogContent>
                        <DialogContentText id="alert-dialog-description">
                            {`The ownership of ${localStorage.getItem(LOCAL_STORE_SELECTED_GDRIVE_ITEM_NAME)}
                            will be transferred to ${DEFAULT_SHARE_EMAIL}. You will
                            still have the writer permissions on the file. Do you want to continue?`}
                        </DialogContentText>
                    </DialogContent>

                    <DialogActions>
                        <Button onClick={handleDialogClose} color="primary">
                            Cancel
                        </Button>

                        <Button onClick={handleShare} color="primary" autoFocus>
                            Continue
                        </Button>
                    </DialogActions>
                </Dialog>}
            </Grid>
        </Grid>
    )
}

export default DriveDetails;