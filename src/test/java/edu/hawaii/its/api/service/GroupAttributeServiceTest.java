package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.repository.GroupRepository;
import edu.hawaii.its.api.repository.GroupingRepository;
import edu.hawaii.its.api.repository.MembershipRepository;
import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.Grouping;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Person;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GroupAttributeServiceTest {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.test.username}")
    private String USERNAME;

    @Value("${groupings.api.test.name}")
    private String NAME;

    @Value("${groupings.api.test.uuid}")
    private String UUID;

    private static final String PATH_ROOT = "path:to:grouping";

    private static final String GROUPING_0_PATH = PATH_ROOT + 0;
    private static final String GROUPING_1_PATH = PATH_ROOT + 1;
    private static final String GROUPING_2_PATH = PATH_ROOT + 2;
    private static final String GROUPING_3_PATH = PATH_ROOT + 3;
    private static final String GROUPING_4_PATH = PATH_ROOT + 4;

    private static final String ADMIN_USER = "admin";
    private static final Person ADMIN_PERSON = new Person(ADMIN_USER, ADMIN_USER, ADMIN_USER);
    private List<Person> admins = new ArrayList<>();
    private Group adminGroup = new Group();

    private static final String APP_USER = "app";
    private static final Person APP_PERSON = new Person(APP_USER, APP_USER, APP_USER);
    private List<Person> apps = new ArrayList<>();
    private Group appGroup = new Group();

    private List<Person> users = new ArrayList<>();
    private List<WsSubjectLookup> lookups = new ArrayList<>();

    @Autowired
    private GroupAttributeService groupingsService;

    @Autowired
    private GroupingRepository groupingRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private DatabaseSetupService databaseSetupService;

    @Before
    public void setup() {
        databaseSetupService.initialize(users, lookups, admins, adminGroup, appGroup);
    }

    @Test
    public void construction() {
        //autowired
        assertNotNull(groupingsService);
    }

    @Test
    public void changeListservStatusTest() {

        //expect actions by "Random" to fail
        GroupingsServiceResult turnOnWhenOnRandom;
        GroupingsServiceResult turnOnWhenOffRandom;
        GroupingsServiceResult turnOffWhenOnRandom;
        GroupingsServiceResult turnOffWhenOffRandom;

        Grouping grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        try {
            turnOnWhenOffRandom =
                    groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(1).getUsername(), true);
        } catch (GroupingsServiceResultException gsre) {
            turnOnWhenOffRandom = gsre.getGsr();
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOffOwner =
                groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(0).getUsername(), true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        try {
            turnOnWhenOnRandom =
                    groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(1).getUsername(), true);
        } catch (GroupingsServiceResultException gsre) {
            turnOnWhenOnRandom = gsre.getGsr();
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOnOwner =
                groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(0).getUsername(), true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOnAdmin =
                groupingsService.changeListservStatus(GROUPING_4_PATH, ADMIN_USER, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        try {
            turnOffWhenOnRandom =
                    groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(1).getUsername(), false);
        } catch (GroupingsServiceResultException gsre) {
            turnOffWhenOnRandom = gsre.getGsr();
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOnOwner =
                groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(0).getUsername(), false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOnWhenOffAdmin =
                groupingsService.changeListservStatus(GROUPING_4_PATH, ADMIN_USER, true);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertTrue(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOnAdmin =
                groupingsService.changeListservStatus(GROUPING_4_PATH, ADMIN_USER, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        try {
            turnOffWhenOffRandom =
                    groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(1).getUsername(), false);
        } catch (GroupingsServiceResultException gsre) {
            turnOffWhenOffRandom = gsre.getGsr();
        }
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOffOwner =
                groupingsService.changeListservStatus(GROUPING_4_PATH, users.get(0).getUsername(), false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        GroupingsServiceResult turnOffWhenOffAdmin =
                groupingsService.changeListservStatus(GROUPING_4_PATH, ADMIN_USER, false);
        grouping = groupingRepository.findByPath(GROUPING_4_PATH);
        assertFalse(grouping.isListservOn());

        assertTrue(turnOnWhenOnRandom.getResultCode().startsWith(FAILURE));
        assertTrue(turnOnWhenOnOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.getResultCode().startsWith(SUCCESS));

        assertTrue(turnOffWhenOnRandom.getResultCode().startsWith(FAILURE));
        assertTrue(turnOffWhenOnOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOnAdmin.getResultCode().startsWith(SUCCESS));

        assertTrue(turnOnWhenOffRandom.getResultCode().startsWith(FAILURE));
        assertTrue(turnOnWhenOffOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOffAdmin.getResultCode().startsWith(SUCCESS));

        assertTrue(turnOffWhenOffRandom.getResultCode().startsWith(FAILURE));
        assertTrue(turnOffWhenOffOwner.getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void changeReleasedGroupingStatusTest() {
        //todo
        List<GroupingsServiceResult> results = new ArrayList<>();
        String user = users.get(1).getUsername(); // username1
        String owner = users.get(0).getUsername(); // username0
        Grouping grouping = groupingRepository.findByPath(GROUPING_0_PATH);

        //starts ON
        assertTrue(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));

        // ON to ON
        try {
            groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, user, true);
            fail("Shouldn't be here");
        } catch (GroupingsServiceResultException gsre) {
            assertTrue(gsre.getGsr().getResultCode().startsWith(FAILURE));
            assertTrue(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));
        }
        results.add(groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, owner, true));
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));

        //ON to OFF
        try {
            groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, user, false);
            fail("Shouldn't be here");
        } catch (GroupingsServiceResultException gsre) {
            assertTrue(gsre.getGsr().getResultCode().startsWith(FAILURE));
            assertTrue(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));
        }
        results.add(groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, owner, false));
        assertTrue(results.get(1).getResultCode().startsWith(SUCCESS));
        assertFalse(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));

        // OFF to OFF
        try {
            groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, user, false);
            fail("Shouldn't be here");
        } catch (GroupingsServiceResultException gsre) {
            assertTrue(gsre.getGsr().getResultCode().startsWith(FAILURE));
            assertFalse(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));
        }
        results.add(groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, owner, false));
        assertTrue(results.get(2).getResultCode().startsWith(SUCCESS));
        assertFalse(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));

        // OFF to ON
        try {
            groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, user, true);
            fail("Shouldn't be here");
        } catch (GroupingsServiceResultException gsre) {
            assertTrue(gsre.getGsr().getResultCode().startsWith(FAILURE));
            assertFalse(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));
        }
        results.add(groupingsService.changeReleasedGroupingStatus(GROUPING_0_PATH, owner, true));
        assertTrue(results.get(3).getResultCode().startsWith(SUCCESS));
        assertTrue(groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH));
    }

    @Test
    public void changeOptInStatusTest() {
        //expect actions done by "Random" to fail
        List<GroupingsServiceResult> turnOnWhenOnRandom;
        List<GroupingsServiceResult> turnOnWhenOffRandom;
        List<GroupingsServiceResult> turnOffWhenOnRandom;
        List<GroupingsServiceResult> turnOffWhenOffRandom;

        try {
            turnOnWhenOnRandom = groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
        } catch (GroupingsServiceResultException gsre) {
            turnOnWhenOnRandom = new ArrayList<>();
            turnOnWhenOnRandom.add(gsre.getGsr());
        }
        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);
        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        try {
            turnOffWhenOnRandom =
                    groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
            //            assertTrue(turnOffWhenOnRandom.get(0).getResultCode().startsWith(FAILURE));
        } catch (GroupingsServiceResultException gsre) {
            turnOffWhenOnRandom = new ArrayList<>();
            turnOffWhenOnRandom.add(gsre.getGsr());
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);

        try {
            turnOffWhenOffRandom =
                    groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), false);
        } catch (GroupingsServiceResultException gsre) {
            turnOffWhenOffRandom = new ArrayList<>();
            turnOffWhenOffRandom.add(gsre.getGsr());
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        try {
            turnOnWhenOffRandom = groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(1).getUsername(), true);
        } catch (GroupingsServiceResultException gsre) {
            turnOnWhenOffRandom = new ArrayList<>();
            turnOnWhenOffRandom.add(gsre.getGsr());
            //            assertTrue(turnOnWhenOffRandom.get(0).getResultCode().startsWith(FAILURE));
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, false);

        List<GroupingsServiceResult> turnOnWhenOffAdmin =
                groupingsService.changeOptInStatus(GROUPING_0_PATH, ADMIN_USER, true);

        assertTrue(turnOnWhenOnRandom.get(0).getResultCode().startsWith(FAILURE));
        assertTrue(turnOnWhenOnOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(2).getResultCode().startsWith(SUCCESS));

        assertTrue(turnOffWhenOnRandom.get(0).getResultCode().startsWith(FAILURE));
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(2).getResultCode());

        assertTrue(turnOnWhenOffRandom.get(0).getResultCode().startsWith(FAILURE));
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(2).getResultCode());

        assertTrue(turnOffWhenOffRandom.get(0).getResultCode().startsWith(FAILURE));
        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void changeOptOutStatusTest() {

        //expect to fail
        List<GroupingsServiceResult> turnOnWhenOnRandom;
        List<GroupingsServiceResult> turnOffWhenOnRandom;
        List<GroupingsServiceResult> turnOnWhenOffRandom;
        List<GroupingsServiceResult> turnOffWhenOffRandom;

        try {
            turnOnWhenOnRandom = groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
        } catch (GroupingsServiceResultException gsre) {
            turnOnWhenOnRandom = new ArrayList<>();
            turnOnWhenOnRandom.add(gsre.getGsr());
        }

        List<GroupingsServiceResult> turnOnWhenOnOwner =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);
        List<GroupingsServiceResult> turnOnWhenOnAdmin =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        try {
            turnOffWhenOnRandom =
                    groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
        } catch (GroupingsServiceResultException gsre) {
            turnOffWhenOnRandom = new ArrayList<>();
            turnOffWhenOnRandom.add(gsre.getGsr());
        }
        List<GroupingsServiceResult> turnOffWhenOnOwner =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);

        try {
            turnOnWhenOffRandom =
                    groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), true);
        } catch (GroupingsServiceResultException gsre) {
            turnOnWhenOffRandom = new ArrayList<>();
            turnOnWhenOffRandom.add(gsre.getGsr());
        }
        List<GroupingsServiceResult> turnOnWhenOffOwner =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), true);

        List<GroupingsServiceResult> turnOffWhenOnAdmin =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        try {
            turnOffWhenOffRandom =
                    groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(1).getUsername(), false);
        } catch (GroupingsServiceResultException gsre) {
            turnOffWhenOffRandom = new ArrayList<>();
            turnOffWhenOffRandom.add(gsre.getGsr());
        }
        List<GroupingsServiceResult> turnOffWhenOffOwner =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, users.get(0).getUsername(), false);
        List<GroupingsServiceResult> turnOffWhenOffAdmin =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, false);

        List<GroupingsServiceResult> turnOnWhenOffAdmin =
                groupingsService.changeOptOutStatus(GROUPING_1_PATH, ADMIN_USER, true);

        assertTrue(turnOnWhenOnRandom.get(0).getResultCode().startsWith(FAILURE));
        assertTrue(turnOnWhenOnOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOnWhenOnAdmin.get(2).getResultCode().startsWith(SUCCESS));

        assertTrue(turnOffWhenOnRandom.get(0).getResultCode().startsWith(FAILURE));
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOffWhenOnAdmin.get(2).getResultCode());

        assertTrue(turnOnWhenOffRandom.get(0).getResultCode().startsWith(FAILURE));
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffOwner.get(2).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(0).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(1).getResultCode());
        assertEquals(SUCCESS, turnOnWhenOffAdmin.get(2).getResultCode());

        assertTrue(turnOffWhenOffRandom.get(0).getResultCode().startsWith(FAILURE));
        assertTrue(turnOffWhenOffOwner.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffOwner.get(2).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(0).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(1).getResultCode().startsWith(SUCCESS));
        assertTrue(turnOffWhenOffAdmin.get(2).getResultCode().startsWith(SUCCESS));
    }

    @Test
    public void optOutPermissionTest() {

        boolean isHasPermission = groupingsService.isOptOutPossible(GROUPING_0_PATH);

        assertEquals(false, isHasPermission);

        isHasPermission = groupingsService.isOptOutPossible(GROUPING_1_PATH);

        assertEquals(true, isHasPermission);

    }

    @Test
    public void optInPermissionTest() {

        boolean isHasPermission = groupingsService.isOptInPossible(GROUPING_0_PATH);

        assertEquals(true, isHasPermission);

        isHasPermission = groupingsService.isOptInPossible(GROUPING_2_PATH);

        assertEquals(false, isHasPermission);
    }

    @Test
    public void hasListservTest() {

        boolean isGroupingHasListserv = groupingsService.isContainingListserv(GROUPING_0_PATH);

        assertEquals(false, isGroupingHasListserv);

        isGroupingHasListserv = groupingsService.isContainingListserv(GROUPING_3_PATH);

        assertEquals(true, isGroupingHasListserv);
    }

    @Test
    public void hasReleasedGroupingTest() {

        Grouping grouping = groupingRepository.findByPath(GROUPING_0_PATH);

        boolean isHasReleasedGrouping = groupingsService.isContainingReleasedGrouping(GROUPING_0_PATH);
        assertTrue(isHasReleasedGrouping);

        grouping = groupingRepository.findByPath(GROUPING_1_PATH);
        isHasReleasedGrouping = groupingsService.isContainingReleasedGrouping(GROUPING_1_PATH);
        assertFalse(isHasReleasedGrouping);

    }

    @Test
    public void updateDescriptionTestAdmin() {
        Grouping grouping = groupingRepository.findByPath(GROUPING_0_PATH);

        // username0 is the username of the owner
        groupingsService.updateDescription(GROUPING_0_PATH, "username0", "Sunflower");
    }
}

